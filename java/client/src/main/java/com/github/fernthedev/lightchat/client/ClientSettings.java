package com.github.fernthedev.lightchat.client;

import com.github.fernthedev.lightchat.core.CoreSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientSettings extends CoreSettings {
    private boolean runNatives = true;


}
