package com.rehabai.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oauth_clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "clientId")
public class OAuthClient {

    @Id
    @Column(name = "client_id", length = 100)
    private String clientId;

    @Column(name = "client_secret", nullable = false, length = 200)
    private String clientSecret;

    @Column(name = "scopes", length = 255)
    private String scopes;

    @Column(name = "grant_types", length = 255)
    private String grantTypes;

    @Column(name = "redirect_uris", length = 500)
    private String redirectUris;
}
