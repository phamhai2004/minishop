import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import voucherApi from "../../api/voucherApi";

import VoucherCard from "../../components/voucher/VoucherCard";
import VoucherConditionModal from "../../components/voucher/VoucherConditionModal";
import VoucherHistoryModal from "../../components/voucher/VoucherHistoryModal";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./VouchersPage.css";

const MY_VOUCHER_TABS = [
  {
    key: "ALL",
    label: "Tất cả",
  },
  {
    key: "AVAILABLE",
    label: "Có thể sử dụng",
  },
  {
    key: "UPCOMING",
    label: "Dùng sau",
  },
  {
    key: "USED",
    label: "Đã sử dụng",
  },
  {
    key: "EXPIRED",
    label: "Hết hạn",
  },
];

function MyVouchersPage() {
  const navigate = useNavigate();
  const [historyOpen, setHistoryOpen] = useState(false);
  const [myVouchers, setMyVouchers] = useState([]);
  const [activeTab, setActiveTab] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedConditionVoucher, setSelectedConditionVoucher] =
    useState(null);

  const loadMyVouchers = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await voucherApi.getMyVouchers();

      setMyVouchers(response.data?.data ?? []);
    } catch (requestError) {
      console.error("Không thể tải voucher của tôi:", requestError);

      setError(
        requestError.response?.data?.message ||
          "Không thể tải Voucher của tôi.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void loadMyVouchers();
  }, [loadMyVouchers]);

  const filteredVouchers = useMemo(() => {
    if (activeTab === "ALL") {
      return myVouchers;
    }

    return myVouchers.filter((voucher) => voucher.status === activeTab);
  }, [activeTab, myVouchers]);

  const handleUseVoucher = (voucher) => {
    if (!voucher || voucher.usableNow !== true) {
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
      <main className="voucher-page">
        <div className="voucher-page__container">
          <LoadingSpinner size="large" />
        </div>
      </main>
    );
  }

  return (
    <main className="voucher-page">
      <div className="voucher-page__container">
        <div className="voucher-page__heading">
          <h1>Voucher của tôi</h1>

          <div className="voucher-page__heading-links">
            <button
              type="button"
              onClick={() => navigate("/customer/vouchers")}
            >
              Tìm thêm voucher
            </button>

            <button type="button" onClick={() => setHistoryOpen(true)}>
              Xem lịch sử voucher
            </button>
          </div>
        </div>

        {error && <div className="voucher-page__error">{error}</div>}

        <div className="voucher-page__tabs" role="tablist">
          {MY_VOUCHER_TABS.map((tab) => (
            <button
              key={tab.key}
              type="button"
              className={activeTab === tab.key ? "is-active" : ""}
              onClick={() => setActiveTab(tab.key)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {filteredVouchers.length === 0 ? (
          <div className="voucher-page__empty">
            Bạn chưa có voucher phù hợp.
          </div>
        ) : (
          <div className="voucher-page__list">
            {filteredVouchers.map((voucher) => (
              <VoucherCard
                key={voucher.id}
                voucher={voucher}
                userVoucher={voucher}
                showQuantity={false}
                onUse={handleUseVoucher}
                onShowCondition={setSelectedConditionVoucher}
              />
            ))}
          </div>
        )}
      </div>

      <VoucherConditionModal
        voucher={selectedConditionVoucher}
        onClose={() => setSelectedConditionVoucher(null)}
      />

      {historyOpen && (
        <VoucherHistoryModal
          vouchers={myVouchers}
          onClose={() => setHistoryOpen(false)}
        />
      )}
    </main>
  );
}

export default MyVouchersPage;
