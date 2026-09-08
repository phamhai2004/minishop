package com.example.minishop.dto.response;

public class ShopFollowStatusResponse {

    private Boolean following;
    private Long totalFollowers;

    public ShopFollowStatusResponse() {
    }

    public ShopFollowStatusResponse(
            Boolean following,
            Long totalFollowers
    ) {
        this.following = following;
        this.totalFollowers = totalFollowers;
    }

    public Boolean getFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    public Long getTotalFollowers() {
        return totalFollowers;
    }

    public void setTotalFollowers(
            Long totalFollowers
    ) {
        this.totalFollowers = totalFollowers;
    }
}