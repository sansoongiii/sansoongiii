package common;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

public final class Protocol {

    public static final int PORT = 50023;
    public static final String SEPARATOR = ":";
    public static final String DATA_SEPARATOR = "|";

    private Protocol() {
    }

    // Message → 한 줄 전송 문자열
    public static String build(Message message) {
        if (message == null || message.getType() == null) {
            throw new IllegalArgumentException(
                    "메시지 또는 메시지 종류가 없습니다."
            );
        }

        String payload;

        switch (message.getType()) {
            case GET_CATEGORIES:
            case CREATE_ROOM:
            case START_GAME:
            case LEAVE_ROOM:
            case DISCONNECT:
                payload = "";
                break;

            case JOIN_ROOM:
                payload = buildData(message.getRoomCode());
                break;

            case SELECT_CATEGORY:
                payload = buildData(message.getCategory());
                break;

            case SUBMIT_TEXT:
                payload = buildData(message.getTextDescription());
                break;
            
            case SAVE_DRAWING_PROGRESS:
            case SUBMIT_DRAWING:
                payload = buildData(
                        encodeImage(message.getDrawingData())
                );
                break;

            case SUBMIT_ANSWER:
                payload = buildData(message.getSubmittedAnswer());
                break;
                
            case SAVE_TEXT_PROGRESS:
                payload = buildData(message.getTextDescription());
                break;
                
            case SAVE_ANSWER_PROGRESS:
                payload = buildData(message.getSubmittedAnswer());
                break;
                
            case CONTINUE_RESPONSE:
                payload = buildData(
                        Boolean.toString(message.isAgree())
                );
                break;

            case CONNECTED:
                payload = buildData(message.getPlayerId());
                break;

            case CATEGORY_LIST:
                payload = buildData(
                        message.getCategories().toArray(new String[0])
                );
                break;

            case ROOM_UPDATED:
                payload = buildData(
                        message.getRoomCode(),
                        Integer.toString(message.getPlayerCount()),
                        message.getCategory()
                );
                break;

            case PHASE_STARTED:
                payload = buildData(
                        message.getPhase(),
                        message.getRole(),
                        Long.toString(message.getDeadlineEpochMillis()),
                        message.getAnswer(),
                        message.getTextDescription(),
                        encodeImage(message.getDrawingData())
                );
                break;

            case ROUND_RESULT:
                payload = buildData(
                        message.getAnswer(),
                        message.getTextDescription(),
                        encodeImage(message.getDrawingData()),
                        message.getSubmittedAnswer(),
                        Boolean.toString(message.isCorrect())
                );
                break;

            case CONTINUE_STATUS:
                payload = buildData(
                        Integer.toString(message.getAgreedCount()),
                        Integer.toString(message.getPlayerCount()),
                        message.getNotice()
                );
                break;

            case ERROR:
                payload = buildData(
                        message.getErrorCode(),
                        message.getNotice()
                );
                break;

            case GAME_FINISHED:
            case ROOM_LEFT:
            case GAME_ABORTED:
            case TEST_MESSAGE:
                payload = buildData(message.getNotice());
                break;

            default:
                throw new IllegalArgumentException(
                        "지원하지 않는 메시지 종류입니다."
                );
        }

        return message.getType().name()
                + SEPARATOR + message.getRoundNumber()
                + SEPARATOR + payload;
    }

    // 한 줄 수신 문자열 → Message
    public static Message parse(String line) {
        if (line == null) {
            throw new IllegalArgumentException(
                    "수신한 메시지가 없습니다."
            );
        }

        String[] header = line.split(
                Pattern.quote(SEPARATOR),
                3
        );

        if (header.length != 3) {
            throw new IllegalArgumentException(
                    "형식: 메시지종류:라운드번호:항목들"
            );
        }

        Message message = new Message();

        message.setType(MessageType.valueOf(header[0]));
        message.setRoundNumber(Integer.parseInt(header[1]));

        String[] values = parseData(header[2]);

        switch (message.getType()) {
            case GET_CATEGORIES:
            case CREATE_ROOM:
            case START_GAME:
            case LEAVE_ROOM:
            case DISCONNECT:
                requireCount(values, 0);
                break;

            case JOIN_ROOM:
                requireCount(values, 1);
                message.setRoomCode(values[0]);
                break;

            case SELECT_CATEGORY:
                requireCount(values, 1);
                message.setCategory(values[0]);
                break;

            case SUBMIT_TEXT:
                requireCount(values, 1);
                message.setTextDescription(values[0]);
                break;
            
            case SAVE_DRAWING_PROGRESS: 
            case SUBMIT_DRAWING:
                requireCount(values, 1);
                message.setDrawingData(decodeImage(values[0]));
                break;

            case SUBMIT_ANSWER:
                requireCount(values, 1);
                message.setSubmittedAnswer(values[0]);
                break;

            case SAVE_TEXT_PROGRESS:
                requireCount(values, 1);
                message.setTextDescription(values[0]);
                break;
                
            case SAVE_ANSWER_PROGRESS:
                requireCount(values, 1);
                message.setSubmittedAnswer(values[0]);
                break;
                
            case CONTINUE_RESPONSE:
                requireCount(values, 1);
                message.setAgree(parseBoolean(values[0]));
                break;

            case CONNECTED:
                requireCount(values, 1);
                message.setPlayerId(values[0]);
                break;

            case CATEGORY_LIST:
                message.setCategories(Arrays.asList(values));
                break;

            case ROOM_UPDATED:
                requireCount(values, 3);
                message.setRoomCode(values[0]);
                message.setPlayerCount(Integer.parseInt(values[1]));
                message.setCategory(values[2]);
                break;

            case PHASE_STARTED:
                requireCount(values, 6);
                message.setPhase(values[0]);
                message.setRole(values[1]);
                message.setDeadlineEpochMillis(
                        Long.parseLong(values[2])
                );
                message.setAnswer(values[3]);
                message.setTextDescription(values[4]);
                message.setDrawingData(decodeImage(values[5]));
                break;

            case ROUND_RESULT:
                requireCount(values, 5);
                message.setAnswer(values[0]);
                message.setTextDescription(values[1]);
                message.setDrawingData(decodeImage(values[2]));
                message.setSubmittedAnswer(values[3]);
                message.setCorrect(parseBoolean(values[4]));
                break;

            case CONTINUE_STATUS:
                requireCount(values, 3);
                message.setAgreedCount(
                        Integer.parseInt(values[0])
                );
                message.setPlayerCount(
                        Integer.parseInt(values[1])
                );
                message.setNotice(values[2]);
                break;

            case ERROR:
                requireCount(values, 2);
                message.setErrorCode(values[0]);
                message.setNotice(values[1]);
                break;

            case GAME_FINISHED:
            case ROOM_LEFT:
            case GAME_ABORTED:
            case TEST_MESSAGE:
                requireCount(values, 1);
                message.setNotice(values[0]);
                break;

            default:
                throw new IllegalArgumentException(
                        "지원하지 않는 메시지 종류입니다."
                );
        }

        return message;
    }

    // 여러 문자열 항목 → 구분자로 연결된 문자열
    public static String buildData(String... values) {
        String[] encoded = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            String value = values[i] == null ? "" : values[i];

            // 빈 문자열도 항목 하나로 식별하기 위한 표시
            encoded[i] = value.isEmpty()
                    ? "~"
                    : URLEncoder.encode(
                            value,
                            StandardCharsets.UTF_8
                    ).replace("~", "%7E");
        }

        return String.join(DATA_SEPARATOR, encoded);
    }

    // 연결된 문자열 → 원래 문자열 항목들
    public static String[] parseData(String payload) {
        if (payload == null || payload.isEmpty()) {
            return new String[0];
        }

        String[] values = payload.split(
                Pattern.quote(DATA_SEPARATOR),
                -1
        );

        for (int i = 0; i < values.length; i++) {
            values[i] = "~".equals(values[i])
                    ? ""
                    : URLDecoder.decode(
                            values[i],
                            StandardCharsets.UTF_8
                    );
        }

        return values;
    }

    private static void requireCount(
            String[] values,
            int expected) {

        if (values.length != expected) {
            throw new IllegalArgumentException(
                    "메시지 항목 수가 올바르지 않습니다. "
                            + "필요: " + expected
                            + ", 실제: " + values.length
            );
        }
    }

    private static boolean parseBoolean(String value) {
        if ("true".equals(value)) {
            return true;
        }

        if ("false".equals(value)) {
            return false;
        }

        throw new IllegalArgumentException(
                "boolean 값은 true 또는 false여야 합니다."
        );
    }

    private static String encodeImage(byte[] image) {
        if (image == null || image.length == 0) {
            return "";
        }

        return Base64.getEncoder().encodeToString(image);
    }

    private static byte[] decodeImage(String value) {
        if (value.isEmpty()) {
            return null;
        }

        return Base64.getDecoder().decode(value);
    }
}