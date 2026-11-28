package common;

public enum MessageType {

    // 클라이언트 → 서버
    GET_CATEGORIES,
    CREATE_ROOM,
    JOIN_ROOM,
    SELECT_CATEGORY,
    START_GAME,
    
    SUBMIT_TEXT,
    SUBMIT_DRAWING,
    SUBMIT_ANSWER,
    
    SAVE_TEXT_PROGRESS,
    SAVE_DRAWING_PROGRESS,
    SAVE_ANSWER_PROGRESS,
    
    CONTINUE_RESPONSE,
    LEAVE_ROOM,
    DISCONNECT,
    RESTART_GAME,  //방장이 재시작 요청

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

    POST_GAME_OPTIONS,  //방장에게 재시작/종료 선택차 표시
    WAITING_FOR_HOST,  //3라운드 후 방장 결정 기다림

    ERROR,
    
    TEST_MESSAGE
}