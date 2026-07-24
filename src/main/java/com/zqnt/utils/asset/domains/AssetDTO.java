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
import java.util.ArrayList;
import java.util.List;
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
	private String systemConnectionString;
	private String liveStreamPushUrl;
	private String liveStreamPullUrl;
	private String externalId;
	private String externalDeviceType;
	private String externalDeviceSubType;
	@Builder.Default
	private List<SubAssetDTO> subAssets = new ArrayList<>();
	@Builder.Default
	private List<AssetPayloadDTO> payloads = new ArrayList<>();
	private UUID organization;
}
