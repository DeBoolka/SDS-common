package ru.mirea.dikanev.nikita.common.server.processor;

public interface Codes {

    int PING_ACTION = 0x81;
    int COMMUNICATION_ACTION = 0x82;
    int RESIZE_ACTION = 0x83;
    int LOGIN_ACTION = 0x84;
    int RECONNECT_ACTION = 0x85;
    int POSITION_ACTION = 0x86;
    int GET_ADDRESS_ACTION = 0x87;
    int SET_ADDRESS_ACTION = 0x88;

}
