package listener.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import guild.AudioPlayerGuildData;
import lombok.Getter;
import reactor.core.publisher.Mono;

public class JoinEventListener implements MessageEventListener {

    @Getter
    private final String command = "join";

    @Override
    public Mono<?> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .zipWith(
                        Mono.justOrEmpty(event.getMember()).flatMap(Member::getVoiceState)
                )
                .flatMap(tuple -> {
                    var audioPlayerGD = tuple.getT1();
                    var voiceState = tuple.getT2();
                    return voiceState.getChannel()
                            .flatMap(vs -> vs.join(spec -> spec.setProvider(audioPlayerGD.getProvider())));
                });
    }
}
