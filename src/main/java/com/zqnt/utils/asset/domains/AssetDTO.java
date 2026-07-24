package com.zqnt.utils.asset.domains;


import com.zqnt.utils.common.proto.AssetConnection;
import com.zqnt.utils.common.proto.AssetTypeEnum;
import com.zqnt.utils.common.proto.AssetVendor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.zequent.framework.services.connector.entities.Asset}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetDTO implements Serializable {
	private UUID id;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
	private String modifiedFrom;
	private String sn;
	private String name;
	private AssetTypeEnum type;
	private AssetVendor vendor;
	private AssetConnection connection;
	private String model;
	private String connectionString;
	private String liveStreamServer;
	private String externalId;
	private String externalDeviceType;
	private String externalDeviceSubType;
	private SubAssetDTO subAsset;
	private UUID organization;
}