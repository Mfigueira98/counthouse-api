package io.github.mfigueira98.counthouse.tenant;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;

    @Column(name = "theme_primary")
    private String themePrimary;

    @Column(name = "theme_logo_url")
    private String themeLogoUrl;

    protected Tenant() {}

    public UUID getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public String getThemePrimary() {
        return themePrimary;
    }

    public String getThemeLogoUrl() {
        return themeLogoUrl;
    }
}
