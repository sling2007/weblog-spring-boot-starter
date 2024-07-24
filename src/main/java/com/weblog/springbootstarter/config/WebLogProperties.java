package com.weblog.springbootstarter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * User: sunling
 * Date: 2024/7/23 13:59
 * Description:
 **/
@ConfigurationProperties(prefix = "weblog")
public class WebLogProperties {

    /**
     * enable web log
     */
    private boolean disable = false;

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }
}
