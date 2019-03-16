package pogo.assistance.data.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Intended to be a flexible definition of region. Right now has 1:1 correspondence with countries.
 */
@RequiredArgsConstructor
public enum  Region {
    CL(":flag_cl:"),
    FR(":flag_fr:"),
    JP(":flag_jp:"),
    NYC(":flag_us:"),
    EXTON(":flag_us:"),
    SG(":flag_sg:");

    @Getter
    private final String flagEmote;
}
