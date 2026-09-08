import axiosClient from "./axiosClient";

const paymentApi = {
  createVnPayPayment: (orderId) => {
    return axiosClient.post(`/api/payments/vnpay/${orderId}`);
  },

  getPaymentResult: (transactionRef) => {
    return axiosClient.get(
      `/api/payments/${encodeURIComponent(transactionRef)}`,
    );
  },
};

export default paymentApi;
