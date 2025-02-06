package me.barnaby.civilisation.chat;

public enum ChatType {

    GLOBAL, // sends to everyone on server
    LOCAL, // sends to everyone in configurable proximity from config e.g 100 blocks
    CIVILISATION, // for now dont implement
    STAFF // only show and allow to change to said channel if they have civilisation.staff



}
