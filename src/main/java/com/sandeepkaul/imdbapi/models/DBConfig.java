/**
 * 
 */
package com.sandeepkaul.imdbapi.models;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @author sandeep.kaul
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DBConfig {

	@NotNull
	private String dbUrl;
	@NotNull
	private String dbUser;
	@NotNull
	private String dbPassword;
	@NotNull
	private Integer idleConnectionTestPeriod;
	@NotNull
	private Boolean testConnectionOnCheckin;
	@NotNull
	private Boolean testConnectionOnCheckout;
	@NotNull
    @Min(1)
    private Integer maxIdleTime;
    @NotNull
    @Min(1)
    private Integer maxIdleTimeExcessConnections;
    @NotNull
    @Min(1)
    private Integer minPoolSize;
    @NotNull
    @Min(1)
    private Integer maxPoolSize;
    @NotNull
    @Min(1)
    private Integer unreturnedConnectionTimeout;
    @NotNull
    @Min(1)
    private Integer initialPoolSize;
}
