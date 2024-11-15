package br.com.thiagomagdalena.googledriveapiservice.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@Table("google_drive_api_credentials")
public class GoogleDriveApiCredentials {

    private String type;
    @Id
    private String projectId;
    private String privateKeyId;
    private String privateKey;
    private String clientEmail;
    private String clientId;
    private String authUri;
    private String tokenUri;
    private String authProviderX509CertUrl;
    private String clientX509CertUrl;
    private String universeDomain;

    @Override
    public String toString() {
        return "{" +
                "type: \"" + type + "\"" +
                ", projectId: \"" + projectId + "\"" +
                ", privateKeyId: \"" + privateKeyId + "\"" +
                ", privateKey: \"" + privateKey + "\"" +
                ", clientEmail: \"" + clientEmail + "\"" +
                ", clientId: \"" + clientId + "\"" +
                ", authUri: \"" + authUri + "\"" +
                ", tokenUri: \"" + tokenUri + "\"" +
                ", authProviderX509CertUrl: \"" + authProviderX509CertUrl + "\"" +
                ", clientX509CertUrl: \"" + clientX509CertUrl + "\"" +
                ", universeDomain: \"" + universeDomain + '\'' +
                '}';
    }
}
