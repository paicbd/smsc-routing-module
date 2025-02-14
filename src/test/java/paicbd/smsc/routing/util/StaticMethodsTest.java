package paicbd.smsc.routing.util;

import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.utils.UtilsEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticMethodsTest {
    @Test
    void testConstructorIsPrivate() throws NoSuchMethodException {
        Constructor<StaticMethods> constructor = StaticMethods.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void getMessageType() {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginProtocol("SS7");
        messageEvent.setImsi(null);
        messageEvent.setMoMessage(true);
        assertEquals(UtilsEnum.MessageType.MESSAGE, StaticMethods.getMessageType(messageEvent));

        messageEvent.setImsi("100223311");
        assertEquals(UtilsEnum.MessageType.DELIVER, StaticMethods.getMessageType(messageEvent));

        messageEvent.setMoMessage(false);
        assertEquals(UtilsEnum.MessageType.MESSAGE, StaticMethods.getMessageType(messageEvent));


        messageEvent.setMoMessage(false);
        messageEvent.setOriginProtocol("HTTP");
        messageEvent.setDlr(true);
        assertEquals(UtilsEnum.MessageType.DELIVER, StaticMethods.getMessageType(messageEvent));

        messageEvent.setMoMessage(true);
        assertEquals(UtilsEnum.MessageType.DELIVER, StaticMethods.getMessageType(messageEvent));


        messageEvent.setDlr(false);
        messageEvent.setOriginProtocol("SMPP");
        assertEquals(UtilsEnum.MessageType.MESSAGE, StaticMethods.getMessageType(messageEvent));

        messageEvent.setMoMessage(true);
        assertEquals(UtilsEnum.MessageType.MESSAGE, StaticMethods.getMessageType(messageEvent));

        messageEvent.setOriginProtocol("UNKNOWN");
        assertThrows(IllegalStateException.class, () -> StaticMethods.getMessageType(messageEvent));
    }
}