import { useEffect, useMemo, useRef, useState } from "react";

import L from "leaflet";

import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";
import {
  reversePhotonAddress,
  searchPhotonAddresses,
} from "../../utils/photonAddress";

import LoadingSpinner from "../common/LoadingSpinner";

import "leaflet/dist/leaflet.css";
import "./AddressMapModal.css";

L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const DEFAULT_POSITION = {
  lat: 21.028511,
  lng: 105.804817,
};

function AddressMapModal({
  open,

  detail = "",
  ward = "",
  province = "",

  latitude = null,
  longitude = null,

  onClose,
  onConfirm,
}) {
  const mapElementRef = useRef(null);

  const mapRef = useRef(null);
  const markerRef = useRef(null);

  const [position, setPosition] = useState(DEFAULT_POSITION);

  const [resolvedAddress, setResolvedAddress] = useState("");

  const [loading, setLoading] = useState(false);
  const [locating, setLocating] = useState(false);

  const [error, setError] = useState("");

  const fullAddress = useMemo(() => {
    return [detail, ward, province, "Việt Nam"].filter(Boolean).join(", ");
  }, [detail, ward, province]);

  const reverseGeocode = async (lat, lng) => {
    try {
      const result = await reversePhotonAddress(lat, lng);

      setResolvedAddress(result?.label ?? "");
    } catch (geocodeError) {
      console.error("Reverse geocoding failed:", geocodeError);

      setResolvedAddress("");
    }
  };

  const searchAddress = async () => {
    if (!detail.trim()) {
      return null;
    }

    try {
      const results = await searchPhotonAddresses({
        keyword: detail,

        ward,
        province,

        limit: 1,
      });

      const first = results[0];

      if (!first) {
        return null;
      }

      setResolvedAddress(first.label ?? "");

      return {
        lat: first.latitude,
        lng: first.longitude,
      };
    } catch (searchError) {
      console.error("Unable to search address:", searchError);

      return null;
    }
  };
  useEffect(() => {
    if (!open || !mapElementRef.current) {
      return;
    }

    let cancelled = false;

    const initializeMap = async () => {
      try {
        setLoading(true);
        setError("");
        setResolvedAddress("");

        let initialPosition = null;

        /*
         * 1. Nếu DB đã có tọa độ
         * → ưu tiên tọa độ đó.
         */
        if (latitude != null && longitude != null) {
          const lat = Number(latitude);
          const lng = Number(longitude);

          if (Number.isFinite(lat) && Number.isFinite(lng)) {
            initialPosition = {
              lat,
              lng,
            };
          }
        }

        if (!initialPosition) {
          initialPosition = await searchAddress();
        }

        if (!initialPosition) {
          try {
            const areaResults = await searchPhotonAddresses({
              keyword: ward || province,

              ward: "",
              province,

              limit: 1,
            });

            const area = areaResults[0];

            if (area) {
              initialPosition = {
                lat: area.latitude,
                lng: area.longitude,
              };

              setResolvedAddress(area.label ?? "");
            }
          } catch (areaError) {
            console.warn("Không xác định được khu vực:", areaError);
          }
        }

        if (!initialPosition) {
          initialPosition = DEFAULT_POSITION;
        }

        if (cancelled || !mapElementRef.current) {
          return;
        }

        setPosition(initialPosition);

        /*
         * Tránh tạo map 2 lần.
         */
        if (mapRef.current) {
          mapRef.current.remove();
          mapRef.current = null;
        }

        const map = L.map(mapElementRef.current, {
          center: [initialPosition.lat, initialPosition.lng],

          zoom: latitude != null && longitude != null ? 18 : 16,

          zoomControl: true,
        });

        mapRef.current = map;

        /*
         * Tile OpenStreetMap.
         */
        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
          maxZoom: 19,

          attribution:
            '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        }).addTo(map);

        /*
         * Marker có thể kéo.
         */
        const marker = L.marker([initialPosition.lat, initialPosition.lng], {
          draggable: true,
        }).addTo(map);

        markerRef.current = marker;

        /*
         * Click map → đổi pin.
         */
        map.on("click", async (event) => {
          const nextPosition = {
            lat: event.latlng.lat,
            lng: event.latlng.lng,
          };

          setPosition(nextPosition);

          marker.setLatLng([nextPosition.lat, nextPosition.lng]);

          await reverseGeocode(nextPosition.lat, nextPosition.lng);
        });

        /*
         * Kéo pin → cập nhật tọa độ.
         */
        marker.on("dragend", async () => {
          const latLng = marker.getLatLng();

          const nextPosition = {
            lat: latLng.lat,
            lng: latLng.lng,
          };

          setPosition(nextPosition);

          await reverseGeocode(nextPosition.lat, nextPosition.lng);
        });

        if (latitude != null && longitude != null) {
          await reverseGeocode(initialPosition.lat, initialPosition.lng);
        }
        setTimeout(() => {
          map.invalidateSize();
        }, 100);
      } catch (initializationError) {
        console.error("Unable to initialize Leaflet map:", initializationError);

        if (!cancelled) {
          setError("Không thể tải bản đồ. Vui lòng kiểm tra kết nối mạng.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void initializeMap();

    return () => {
      cancelled = true;

      if (mapRef.current) {
        mapRef.current.remove();
      }

      mapRef.current = null;
      markerRef.current = null;
    };
  }, [open, latitude, longitude, fullAddress]);

  if (!open) {
    return null;
  }

  const handleCurrentLocation = () => {
    if (!navigator.geolocation) {
      setError("Trình duyệt không hỗ trợ lấy vị trí hiện tại.");

      return;
    }

    setLocating(true);
    setError("");

    navigator.geolocation.getCurrentPosition(
      async (result) => {
        const nextPosition = {
          lat: result.coords.latitude,
          lng: result.coords.longitude,
        };

        setPosition(nextPosition);

        if (markerRef.current) {
          markerRef.current.setLatLng([nextPosition.lat, nextPosition.lng]);
        }

        if (mapRef.current) {
          mapRef.current.setView([nextPosition.lat, nextPosition.lng], 18);
        }

        await reverseGeocode(nextPosition.lat, nextPosition.lng);

        setLocating(false);
      },

      (locationError) => {
        console.error("Unable to get current location:", locationError);

        setError(
          "Không thể lấy vị trí hiện tại. Hãy cấp quyền vị trí cho trình duyệt.",
        );

        setLocating(false);
      },

      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      },
    );
  };

  const handleConfirm = () => {
    onConfirm?.({
      latitude: position.lat,
      longitude: position.lng,
      formattedAddress: resolvedAddress,
    });
  };

  return (
    <div
      className="address-map-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="address-map-title"
    >
      <div className="address-map-modal__overlay" onClick={onClose} />

      <div className="address-map-modal__dialog">
        <header className="address-map-modal__header">
          <button
            type="button"
            className="address-map-modal__back"
            onClick={onClose}
            aria-label="Quay lại"
          >
            ←
          </button>

          <h2 id="address-map-title">Xác nhận địa chỉ</h2>

          <button
            type="button"
            className="address-map-modal__close"
            onClick={onClose}
            aria-label="Đóng"
          >
            ×
          </button>
        </header>

        <div className="address-map-modal__address">
          <strong>Địa chỉ đã nhập</strong>

          <span>{fullAddress || "Chưa có địa chỉ"}</span>
        </div>

        <div className="address-map-modal__map-wrapper">
          <div ref={mapElementRef} className="address-map-modal__map" />

          {loading && <LoadingSpinner size="large" overlay />}

          <button
            type="button"
            className="address-map-modal__current-location"
            onClick={handleCurrentLocation}
            disabled={locating || loading}
            title="Sử dụng vị trí hiện tại"
            aria-label="Sử dụng vị trí hiện tại"
          >
            ◎
          </button>
        </div>

        <div className="address-map-modal__selected">
          <div>
            <strong> Vị trí trên bản đồ</strong>

            {resolvedAddress && <p>{resolvedAddress}</p>}

            <span>
              {Number(position.lat).toFixed(6)},{" "}
              {Number(position.lng).toFixed(6)}
            </span>
          </div>

          <small>
            Nhấn vào bản đồ hoặc kéo ghim để điều chỉnh vị trí chính xác.
          </small>
        </div>

        {error && (
          <div className="address-map-modal__error" role="alert">
            {error}
          </div>
        )}

        <footer className="address-map-modal__footer">
          <button
            type="button"
            className="address-map-modal__cancel"
            onClick={onClose}
          >
            Hủy
          </button>

          <button
            type="button"
            className="address-map-modal__confirm"
            onClick={handleConfirm}
            disabled={loading}
          >
            Xác nhận vị trí
          </button>
        </footer>
      </div>
    </div>
  );
}

export default AddressMapModal;
