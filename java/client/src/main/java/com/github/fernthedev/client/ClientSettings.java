package com.github.fernthedev.client;

import com.github.fernthedev.core.CoreSettings;
import com.github.fernthedev.core.encryption.codecs.CodecEnum;
import com.github.fernthedev.core.encryption.codecs.JSONHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientSettings extends CoreSettings {
    private boolean runNatives = true;

    private JSONHandler jsonHandler = CodecEnum.GSON.getJsonHandler();
}
