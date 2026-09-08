import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const defaultConfig = {
  baseURL: API_BASE_URL,
  timeout: 20000,
};

export const publicClient = axios.create(defaultConfig);

export const authenticatedClient = axios.create(defaultConfig);

export { API_BASE_URL };
