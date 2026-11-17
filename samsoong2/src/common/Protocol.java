package common;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class Protocol {
	
	public static final int PORT = 50023;
    public static final String SEPARATOR = ":";
    public static final String DATA_SEPARATOR = "|";

    private Protocol() {
    }

    // Message → 전송 문자열
    public static String build(Message message) {
        if (message == null) {
            throw new IllegalArgumentException(
                    "전송할 메시지가 없습니다."
            );
        }

        return message.getType().name()
                + SEPARATOR + message.getRoundNumber()
                + SEPARATOR + escape(message.getData());
    }

    // 수신 문자열 → Message
    public static Message parse(String line) {
        if (line == null) {
            throw new IllegalArgumentException(
                    "수신한 메시지가 없습니다."
            );
        }

        String[] parts = line.split(":", 3);

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "형식: 메시지종류:라운드번호:데이터"
            );
        }

        MessageType type;

        try {
            type = MessageType.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "지원하지 않는 메시지 종류입니다: " + parts[0],
                    e
            );
        }

        int roundNumber;

        try {
            roundNumber = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "라운드 번호는 숫자여야 합니다.",
                    e
            );
        }

        return new Message(
                type,
                roundNumber,
                unescape(parts[2])
        );
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String unescape(String value) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);

            if (current != '\\') {
                result.append(current);
                continue;
            }

            if (i + 1 >= value.length()) {
                throw new IllegalArgumentException(
                        "잘못된 이스케이프 문자입니다."
                );
            }

            char next = value.charAt(++i);

            switch (next) {
                case '\\':
                    result.append('\\');
                    break;

                case 'r':
                    result.append('\r');
                    break;

                case 'n':
                    result.append('\n');
                    break;

                default:
                    throw new IllegalArgumentException(
                            "지원하지 않는 이스케이프 문자입니다."
                    );
            }
        }

        return result.toString();
    }
    
    // 여러 항목 → Message.data 문자열
    public static String buildData(String... values) {
        String[] encoded = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            String value = values[i] == null ? "" : values[i];

            encoded[i] = URLEncoder.encode(
                    value,
                    StandardCharsets.UTF_8
            );
        }

        return String.join(DATA_SEPARATOR, encoded);
    }
    
    // Message.data 문자열 → 항목 배열
    public static String[] parseData(String data) {
        if (data == null || data.isEmpty()) {
            return new String[0];
        }

        String[] values = data.split(
                Pattern.quote(DATA_SEPARATOR),
                -1
        );

        for (int i = 0; i < values.length; i++) {
            values[i] = URLDecoder.decode(
                    values[i],
                    StandardCharsets.UTF_8
            );
        }

        return values;
    }
}