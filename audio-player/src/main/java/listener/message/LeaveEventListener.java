package listener.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.voice.VoiceConnection;
import lombok.Getter;
import reactor.core.publisher.Mono;

public class LeaveEventListener implements MessageEventListener {

    @Getter
    private final String command = "leave";

    @Override
    public Mono<?> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(voiceState -> event.getClient()
                        .getVoiceConnectionRegistry()
                        .getVoiceConnection(voiceState.getGuildId()))
                .flatMap(VoiceConnection::disconnect);
    }
}
