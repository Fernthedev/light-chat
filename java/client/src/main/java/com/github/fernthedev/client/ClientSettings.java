package com.github.fernthedev.client;

import com.github.fernthedev.core.CoreSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientSettings extends CoreSettings {
    private boolean runNatives = true;


}
