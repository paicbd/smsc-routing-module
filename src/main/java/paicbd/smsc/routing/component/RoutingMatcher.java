package paicbd.smsc.routing.component;

import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.RoutingRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoutingMatcher {
    private final ConcurrentMap<Integer, List<RoutingRule>> routingRules;

    public RoutingRule getRouting(MessageEvent messageEvent) {
        if (Objects.isNull(this.routingRules.get(messageEvent.getOriginNetworkId()))) {
            return null;
        }
        Optional<RoutingRule> optionalRouting = this.routingRules.get(messageEvent.getOriginNetworkId())
                .stream()
                .filter(routing -> this.matchesRoutingCriteria(messageEvent, routing))
                .findFirst();
        return optionalRouting.orElse(null);
    }

    private boolean matchesRoutingCriteria(MessageEvent messageEvent, RoutingRule routing) {
        log.debug("Checking Rule with id {}", routing.getId());
        if (!routing.isHasFilterRules()) {
            return true;
        }

        return this.matchesRegex(messageEvent.getSourceAddr(), routing.getOriginRegexSourceAddr(), "source address") &&
                this.matchesRegex(String.valueOf(messageEvent.getSourceAddrTon()), routing.getOriginRegexSourceAddrTon(), "source address TON") &&
                this.matchesRegex(String.valueOf(messageEvent.getSourceAddrNpi()), routing.getOriginRegexSourceAddrNpi(), "source address NPI") &&
                this.matchesRegex(messageEvent.getDestinationAddr(), routing.getOriginRegexDestinationAddr(), "destination address") &&
                this.matchesRegex(String.valueOf(messageEvent.getDestAddrTon()), routing.getOriginRegexDestAddrTon(), "destination address TON") &&
                this.matchesRegex(String.valueOf(messageEvent.getDestAddrNpi()), routing.getOriginRegexDestAddrNpi(), "destination address NPI") &&
                this.matchesRegex(String.valueOf(messageEvent.getImsi()), routing.getRegexImsiDigitsMask(), "IMSI Digits") &&
                this.matchesRegex(String.valueOf(messageEvent.getNetworkNodeNumber()), routing.getRegexNetworkNodeNumber(), "Network Node Number") &&
                this.matchesRegex(String.valueOf(messageEvent.getSccpCallingPartyAddress()), routing.getRegexCallingPartyAddress(), "SCCP Calling Party Address") &&
                routing.isSriResponse() == messageEvent.isSriResponse();
    }

    private boolean matchesRegex(String value, String regex, String validation) {
        if (!regex.isEmpty() && !value.matches(regex.replace("\\\\", "\\"))) {
            log.debug("Regex {} does not match value {} trying to validate {}", regex, value, validation);
            return false;
        }
        log.debug("No regex found or it matches");
        return true;
    }
}
