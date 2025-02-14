package paicbd.smsc.routing.util;

import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.utils.UtilsEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class StaticMethods {
    private StaticMethods() {
        throw new IllegalStateException("Utility class");
    }

    public static UtilsEnum.MessageType getMessageType(MessageEvent event) {
        UtilsEnum.MessageType messageType;
        switch (event.getOriginProtocol()) {
            case "SS7" -> {
                if (Objects.isNull(event.getImsi())) {
                    messageType = UtilsEnum.MessageType.MESSAGE;
                } else {
                    messageType = (event.isMoMessage()) ? UtilsEnum.MessageType.DELIVER : UtilsEnum.MessageType.MESSAGE;
                }
            }
            case "HTTP", "SMPP" ->
                    messageType = (event.isDlr()) ? UtilsEnum.MessageType.DELIVER : UtilsEnum.MessageType.MESSAGE;
            default -> throw new IllegalStateException("Unexpected value: " + event.getOriginProtocol());
        }
        return messageType;
    }
}
