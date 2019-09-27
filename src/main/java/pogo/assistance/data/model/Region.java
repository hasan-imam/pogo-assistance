package pogo.assistance.data.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Intended to be a flexible definition of region. Right now has 1:1 correspondence with countries.
 */
@RequiredArgsConstructor
public enum  Region {
    CL(":flag_cl:"),
    FL(":flag_us:"),
    FR(":flag_fr:"),
    JP(":flag_jp:"),
    NYC(":flag_us:"),
    EXTON(":flag_us:"),
    SG(":flag_sg:"),
    SYD(":flag_au:"),
    YVR(":flag_ca:");

    @Getter
    private final String flagEmote;
}
