package com.zqnt.utils.protos;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.Descriptors.FileDescriptor;
import com.zqnt.protos.asset.v3.Asset;
import com.zqnt.protos.common.v3.AssetRef;
import org.junit.jupiter.api.Test;

class PublicContractsFromApiTest {

    @Test
    void internalContractsLinkThePublicDescriptorsOfZqntApi() {
        FileDescriptor common = AssetRef.getDescriptor().getFile();
        FileDescriptor commonSeenByAsset = Asset.getDescriptor().getFile().getDependencies().stream()
                .filter(dependency -> dependency.getName().equals(common.getName()))
                .findFirst()
                .orElseThrow();

        assertSame(common, commonSeenByAsset);
    }

    @Test
    void publicClassesComeFromTheApiArtifact() {
        String location = AssetRef.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        assertTrue(location.contains("zqnt-api-java"), location);
    }
}
