package ru.mirea.dikanev.nikita.common.server.secure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Credentials {

    private String login;
    private String password;

}
