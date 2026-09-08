import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import voucherApi from "../../api/voucherApi";

import VoucherCard from "../../components/voucher/VoucherCard";
import VoucherConditionModal from "../../components/voucher/VoucherConditionModal";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./VouchersPage.css";

function MyVouchersPage() {
  const navigate = useNavigate();
  const [catalog, setCatalog] = useState([]);
  const [myVouchers, setMyVouchers] = useState([]);
  const [activeTab, setActiveTab] = useState("ALL");
  const [voucherCode, setVoucherCode] = useState("");
  const [loading, setLoading] = useState(true);
  const [collectingCode, setCollectingCode] = useState(null);
  const [error, setError] = useState("");
  const [selectedConditionVoucher, setSelectedConditionVoucher] =
    useState(null);
  const loadVouchers = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const [catalogResponse, myVoucherResponse] = await Promise.all([
        voucherApi.getCatalog(),
        voucherApi.getMyVouchers(),
      ]);

      setCatalog(catalogResponse.data?.data ?? []);

      setMyVouchers(myVoucherResponse.data?.data ?? []);
    } catch (requestError) {
      console.error("Không thể tải voucher:", requestError);

      setError(
        requestError.response?.data?.message || "Không thể tải kho voucher.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void loadVouchers();
  }, [loadVouchers]);

  const savedVoucherMap = useMemo(
    () => new Map(myVouchers.map((item) => [item.voucherId, item])),
    [myVouchers],
  );

  const filteredCatalog = useMemo(() => {
    if (activeTab === "PLATFORM") {
      return catalog.filter((voucher) => voucher.scope === "PLATFORM");
    }

    if (activeTab === "SHOP") {
      return catalog.filter((voucher) => voucher.scope === "SHOP");
    }

    return catalog;
  }, [activeTab, catalog]);

  const handleCollect = async (code) => {
    if (!code?.trim()) {
      return;
    }

    try {
      setCollectingCode(code);
      setError("");

      await voucherApi.collect(code.trim());

      await loadVouchers();

      setVoucherCode("");
    } catch (requestError) {
      console.error("Không thể lưu voucher:", requestError);

      setError(
        requestError.response?.data?.message || "Không thể lưu voucher.",
      );
    } finally {
      setCollectingCode(null);
    }
  };

  const handleSubmitCode = (event) => {
    event.preventDefault();

    void handleCollect(voucherCode);
  };

  const handleUseVoucher = (voucher) => {
    if (!voucher) {
      return;
    }

    if (voucher.scope === "SHOP" && voucher.shopId != null) {
      navigate(`/shops/${voucher.shopId}`, {
        state: {
          voucherCode: voucher.code,
        },
      });

      return;
    }

    navigate("/products", {
      state: {
        voucherCode: voucher.code,
      },
    });
  };

  if (loading) {
    return (
      <main>
        <LoadingSpinner size="large" />
      </main>
    );
  }

  return (
    <main className="voucher-page">
      <div className="voucher-page__container">
        <div className="voucher-page__heading">
          <h1>Kho Voucher</h1>

          <div className="voucher-page__heading-links">
            <button
              type="button"
              onClick={() => navigate("/customer/my-vouchers")}
            >
              Voucher của tôi
            </button>
          </div>
        </div>

        <form className="voucher-page__code-form" onSubmit={handleSubmitCode}>
          <label htmlFor="voucher-code">Mã Voucher</label>

          <input
            id="voucher-code"
            type="text"
            value={voucherCode}
            onChange={(event) => setVoucherCode(event.target.value)}
            placeholder="Nhập mã voucher tại đây"
          />

          <button
            type="submit"
            disabled={!voucherCode.trim() || collectingCode !== null}
          >
            {collectingCode !== null ? (
              <LoadingSpinner size="small" inline variant="light" />
            ) : (
              "Lưu"
            )}
          </button>
        </form>

        {error && <div className="voucher-page__error">{error}</div>}

        <div className="voucher-page__tabs" role="tablist">
          <button
            type="button"
            className={activeTab === "ALL" ? "is-active" : ""}
            onClick={() => setActiveTab("ALL")}
          >
            Tất cả
          </button>

          <button
            type="button"
            className={activeTab === "PLATFORM" ? "is-active" : ""}
            onClick={() => setActiveTab("PLATFORM")}
          >
            Hair
          </button>

          <button
            type="button"
            className={activeTab === "SHOP" ? "is-active" : ""}
            onClick={() => setActiveTab("SHOP")}
          >
            Shop
          </button>
        </div>

        {filteredCatalog.length === 0 ? (
          <div className="voucher-page__empty">
            Hiện chưa có voucher phù hợp.
          </div>
        ) : (
          <div className="voucher-page__list">
            {filteredCatalog.map((voucher) => {
              const userVoucher = savedVoucherMap.get(voucher.id) ?? null;

              return (
                <VoucherCard
                  key={voucher.id}
                  voucher={voucher}
                  userVoucher={userVoucher}
                  collecting={collectingCode === voucher.code}
                  onCollect={handleCollect}
                  onUse={handleUseVoucher}
                  onShowCondition={setSelectedConditionVoucher}
                />
              );
            })}
          </div>
        )}
      </div>
      <VoucherConditionModal
        voucher={selectedConditionVoucher}
        onClose={() => setSelectedConditionVoucher(null)}
      />
    </main>
  );
}

export default MyVouchersPage;
