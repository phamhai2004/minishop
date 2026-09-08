import HomeBanner from "../../components/home/HomeBanner";
import HomeCategories from "../../components/home/HomeCategories";
import HomeFlashSale from "../../components/home/HomeFlashSale";
import RecommendedProducts from "../../components/product/RecommendedProducts";

import "./HomePage.css";

function HomePage() {
  return (
    <main className="home-page">
      <HomeBanner />

      <HomeCategories />

      <HomeFlashSale />

      <RecommendedProducts />
    </main>
  );
}

export default HomePage;
