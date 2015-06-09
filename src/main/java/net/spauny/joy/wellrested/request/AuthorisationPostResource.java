package net.spauny.joy.wellrested.request;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 *
 * @author iulian.dafinoiu
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class AuthorisationPostResource implements Serializable {
    private static final long serialVersionUID = 189745645678978978L;
    @XmlElement(name="grant_type")
    @SerializedName("grant_type")
    private String grantType;
    
    @XmlElement(name="client_id")
    @SerializedName("client_id")
    private String clientId;
    
    @XmlElement(name="client_secret")
    @SerializedName("client_secret")
    private String clientSecret;
    
    @XmlElement(name="code")
    @SerializedName("code")
    private String code;
    
    @XmlElement(name="redirect_uri")
    @SerializedName("redirect_uri")
    private String redirectUri;
    
    @XmlElement(name="refresh_token")
    @SerializedName("refresh_token")
    private String refreshToken;
    
}
