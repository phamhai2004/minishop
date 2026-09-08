const PHOTON_BASE_URL = "https://photon.komoot.io";

const searchCache = new Map();

function normalizeText(value = "") {
  return String(value)
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase()
    .replace(/\b(tinh|thanh pho|tp|xa|phuong|thi tran|huyen|quan)\b/g, " ")
    .replace(/[^\p{L}\p{N}\s]/gu, " ")
    .replace(/\s+/g, " ")
    .trim();
}

function uniqueParts(parts) {
  const result = [];
  const seen = new Set();

  for (const part of parts) {
    const value = String(part ?? "").trim();

    if (!value) {
      continue;
    }

    const normalized = normalizeText(value);

    if (seen.has(normalized)) {
      continue;
    }

    seen.add(normalized);
    result.push(value);
  }

  return result;
}

function featureToSuggestion(feature) {
  const properties = feature?.properties ?? {};
  const coordinates = feature?.geometry?.coordinates ?? [];
  const longitude = Number(coordinates[0]);
  const latitude = Number(coordinates[1]);

  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    return null;
  }

  const streetLine = [properties.housenumber, properties.street]
    .filter(Boolean)
    .join(" ");

  const detailParts = uniqueParts([properties.name, streetLine]);

  const detail =
    detailParts.join(", ") || properties.name || properties.street || "";

  const label = uniqueParts([
    detail,
    properties.locality,
    properties.district,
    properties.city,
    properties.county,
    properties.state,
    properties.postcode,
    properties.country,
  ]).join(", ");

  const context = normalizeText(
    [
      properties.name,
      properties.street,
      properties.locality,
      properties.district,
      properties.city,
      properties.county,
      properties.state,
      properties.country,
    ].join(" "),
  );

  return {
    latitude,
    longitude,

    detail,
    label,

    context,

    countryCode: properties.countrycode?.toUpperCase() ?? "",

    raw: feature,
  };
}

export async function searchPhotonAddresses({
  keyword,
  province,
  ward,
  limit = 5,
  signal,
}) {
  const cleanKeyword = keyword?.trim() ?? "";

  if (!cleanKeyword) {
    return [];
  }

  const query = [cleanKeyword, ward, province, "Việt Nam"]
    .filter(Boolean)
    .join(", ");

  const cacheKey = `${normalizeText(query)}|${limit}`;

  if (searchCache.has(cacheKey)) {
    return searchCache.get(cacheKey);
  }

  const params = new URLSearchParams({
    q: query,
    limit: "12",
  });

  const response = await fetch(`${PHOTON_BASE_URL}/api/?${params.toString()}`, {
    signal,
  });

  if (!response.ok) {
    throw new Error("Không thể tìm kiếm địa chỉ.");
  }

  const data = await response.json();
  const features = Array.isArray(data?.features) ? data.features : [];
  const provinceKey = normalizeText(province);
  const wardKey = normalizeText(ward);
  const provinceMatches = features
    .map(featureToSuggestion)
    .filter(Boolean)
    .filter((item) => !item.countryCode || item.countryCode === "VN")
    .filter((item) => {
      if (!provinceKey) {
        return true;
      }

      return item.context.includes(provinceKey);
    });

  const wardMatches = wardKey
    ? provinceMatches.filter((item) => item.context.includes(wardKey))
    : provinceMatches;

  const candidates =
    wardKey && wardMatches.length > 0 ? wardMatches : provinceMatches;

  const suggestions = candidates.slice(0, limit);

  searchCache.set(cacheKey, suggestions);

  return suggestions;
}

export async function reversePhotonAddress(latitude, longitude, signal) {
  const params = new URLSearchParams({
    lat: String(latitude),
    lon: String(longitude),
    limit: "1",
  });

  const response = await fetch(
    `${PHOTON_BASE_URL}/reverse?${params.toString()}`,
    {
      signal,
    },
  );

  if (!response.ok) {
    throw new Error("Không thể xác định địa chỉ từ tọa độ.");
  }

  const data = await response.json();

  const firstFeature = data?.features?.[0];

  return firstFeature ? featureToSuggestion(firstFeature) : null;
}
