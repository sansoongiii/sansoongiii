package common;

public enum MessageType {

    // 클라이언트 → 서버
    GET_CATEGORIES,
    CREATE_ROOM,
    JOIN_ROOM,
    SELECT_CATEGORY,

    SUBMIT_TEXT,
    SUBMIT_DRAWING,
    SUBMIT_ANSWER,

    CONTINUE_RESPONSE,
    LEAVE_ROOM,

    // 서버 → 클라이언트
    CONNECTED,
    CATEGORY_LIST,
    ROOM_UPDATED,

    PHASE_STARTED,
    ROUND_RESULT,
    GAME_FINISHED,

    CONTINUE_STATUS,
    ROOM_LEFT,
    GAME_ABORTED,

    ERROR
}