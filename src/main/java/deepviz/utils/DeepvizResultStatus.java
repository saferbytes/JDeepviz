package deepviz.utils;

public enum DeepvizResultStatus {
    DEEPVIZ_STATUS_SUCCESS,             // Request succesfully submitted
    DEEPVIZ_STATUS_INPUT_ERROR,
    DEEPVIZ_STATUS_CLIENT_ERROR,        // HTTP 4xx
    DEEPVIZ_STATUS_SERVER_ERROR,        // HTTP 5xx
    DEEPVIZ_STATUS_NETWORK_ERROR,       // Cannot contact Deepviz
    DEEPVIZ_STATUS_INTERNAL_ERROR,
}