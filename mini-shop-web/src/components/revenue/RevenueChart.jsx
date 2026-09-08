import { useEffect, useState } from "react";

import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import LoadingSpinner from "../common/LoadingSpinner";

import "./RevenueChart.css";

const PERIODS = [
  {
    value: "DAY",
    label: "Ngày",
  },
  {
    value: "WEEK",
    label: "Tuần",
  },
  {
    value: "MONTH",
    label: "Tháng",
  },
  {
    value: "YEAR",
    label: "Năm",
  },
];

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function getDescription(period) {
  const descriptions = {
    DAY: "Doanh thu đã ghi nhận trong 7 ngày gần nhất.",
    WEEK: "Doanh thu đã ghi nhận trong 8 tuần gần nhất.",
    MONTH: "Doanh thu đã ghi nhận theo từng tháng trong năm hiện tại.",
    YEAR: "Doanh thu đã ghi nhận trong 5 năm gần nhất.",
  };

  return descriptions[period] ?? "";
}

function getSummaryLabel(period, label) {
  if (period === "MONTH") {
    return `Tháng ${String(label).replace("T", "")}`;
  }

  if (period === "WEEK") {
    return `Tuần bắt đầu ${label}`;
  }

  if (period === "DAY") {
    return `Ngày ${label}`;
  }

  return `Năm ${label}`;
}

function RevenueChart({ loadRevenue, initialPeriod = "MONTH" }) {
  const [period, setPeriod] = useState(initialPeriod);

  const [data, setData] = useState([]);

  const [loading, setLoading] = useState(false);

  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadData = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await loadRevenue(period);

        const apiResponse = response.data;

        if (!apiResponse?.success || !Array.isArray(apiResponse?.data)) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải dữ liệu doanh thu.",
          );
        }

        if (!cancelled) {
          setData(
            apiResponse.data.map((item) => ({
              label: item.label,
              revenue: Number(item.revenue ?? 0),
            })),
          );
        }
      } catch (err) {
        console.error("Unable to load revenue chart:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải dữ liệu doanh thu.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadData();

    return () => {
      cancelled = true;
    };
  }, [period, loadRevenue]);

  return (
    <section className="revenue-chart">
      <div className="revenue-chart__heading">
        <div>
          <h2>Biểu đồ doanh thu</h2>

          <p>{getDescription(period)}</p>
        </div>
      </div>

      <div className="revenue-chart__toolbar">
        {PERIODS.map((item) => (
          <button
            key={item.value}
            type="button"
            className={
              period === item.value
                ? "revenue-chart__filter active"
                : "revenue-chart__filter"
            }
            onClick={() => setPeriod(item.value)}
          >
            {item.label}
          </button>
        ))}
      </div>

      {loading && (
        <div className="revenue-chart__state">
          <LoadingSpinner size="medium" />
        </div>
      )}

      {!loading && error && <div className="revenue-chart__error">{error}</div>}

      {!loading && !error && (
        <>
          <div className="revenue-chart__wrapper">
            <ResponsiveContainer width="100%" height="100%" minWidth={0}>
              <LineChart
                data={data}
                margin={{
                  top: 12,
                  right: 20,
                  left: 8,
                  bottom: 6,
                }}
              >
                <CartesianGrid stroke="#e2e8f0" strokeDasharray="3 3" />

                <XAxis
                  dataKey="label"
                  stroke="#94a3b8"
                  tick={{
                    fill: "#64748b",
                    fontSize: 11,
                  }}
                />

                <YAxis
                  width={80}
                  stroke="#94a3b8"
                  tick={{
                    fill: "#64748b",
                    fontSize: 11,
                  }}
                  tickFormatter={(value) => {
                    const number = Number(value ?? 0);

                    if (number >= 1_000_000) {
                      return `${Number((number / 1_000_000).toFixed(1))}tr`;
                    }

                    if (number >= 1_000) {
                      return `${Number((number / 1_000).toFixed(1))}k`;
                    }

                    return number;
                  }}
                />

                <Tooltip
                  formatter={(value) => [formatCurrency(value), "Doanh thu"]}
                  labelFormatter={(label) => getSummaryLabel(period, label)}
                />

                <Line
                  type="monotone"
                  dataKey="revenue"
                  stroke="#8b5cf6"
                  strokeWidth={3}
                  dot={{
                    r: 4,
                    fill: "#a855f7",
                    stroke: "#ffffff",
                    strokeWidth: 2,
                  }}
                  activeDot={{
                    r: 6,
                    fill: "#7e22ce",
                    stroke: "#ffffff",
                    strokeWidth: 3,
                  }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>

          <div className="revenue-chart__summary">
            {data.length > 0 ? (
              data
                .filter((item) => Number(item.revenue ?? 0) > 0)
                .map((item) => (
                  <div key={item.label} className="revenue-chart__summary-item">
                    <span>{getSummaryLabel(period, item.label)}</span>

                    <strong>{formatCurrency(item.revenue)}</strong>
                  </div>
                ))
            ) : (
              <div className="revenue-chart__empty">
                Chưa có dữ liệu doanh thu.
              </div>
            )}

            {data.length > 0 &&
              data.every((item) => Number(item.revenue ?? 0) === 0) && (
                <div className="revenue-chart__empty">
                  Chưa phát sinh doanh thu trong khoảng thời gian này.
                </div>
              )}
          </div>
        </>
      )}
    </section>
  );
}

export default RevenueChart;
