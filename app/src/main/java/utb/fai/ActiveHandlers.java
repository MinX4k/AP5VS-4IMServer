package utb.fai;

import java.util.*;

public class ActiveHandlers {
    private static final long serialVersionUID = 1L;
    private HashSet<SocketHandler> activeHandlersSet = new HashSet<SocketHandler>();

    /**
     * sendMessageToAll - Pole zprávu vem aktivním klientùm kromì sebe sama
     * 
     * @param sender  - reference odesílatele
     * @param message - øetìzec se zprávou
     */
    synchronized void sendMessageToAll(SocketHandler sender, String message) {
        String formatted = "[" + sender.username + "] >> " + message;
        for (SocketHandler handler : activeHandlersSet) {
            if (handler != sender) {
                if (!handler.messages.offer(formatted)) {
                    System.err.printf("Client %s message queue is full, dropping the message!\n", handler.clientID);
                }
            }
        }
    }

    synchronized void sendGroupMessage(SocketHandler sender, List<String> groups, String message) {
        String formatted = "[" + sender.username + "] >> " + message;
        for (SocketHandler handler : activeHandlersSet) {
            if (handler.username == null) continue;

            for (String group : groups) {
                if (handler.groups.contains(group)) {
                    if (!handler.messages.offer(formatted)) {
                        System.err.printf("Client %s message queue is full, dropping the message!\n", handler.clientID);
                    }
                    break;
                }
            }
        }
    }

    synchronized void sendPrivateMessage(SocketHandler sender, String recipient, String message) {
        if (sender.username == null || recipient == null || message == null) {
            System.err.println("sendPrivateMessage: Missing data, skipping send.");
            return;
        }

    String formatted = "[" + sender.username + "] >> " + message;
    boolean delivered = false;

    for (SocketHandler handler : activeHandlersSet) {
        if (handler.username != null && handler.username.equals(recipient)) {
            if (!handler.messages.offer(formatted)) {
                System.err.printf("Client %s message queue is full, dropping the message!\n", handler.clientID);
            } else {
                delivered = true;
            }
        }
    }

    if (!delivered) {
        sender.messages.offer("User '" + recipient + "' not found or not connected.");
    }
}

    synchronized boolean userExists(String username) {
        for (SocketHandler handler : activeHandlersSet) {
            if (Objects.equals(handler.username, username)) {
                return true;
            }
        }
        return false;
    }


    /**
     * add pøidá do mnoiny aktivních handlerù nový handler.
     * Metoda je sychronizovaná, protoe HashSet neumí multithreading.
     * 
     * @param handler - reference na handler, který se má pøidat.
     * @return true if the set did not already contain the specified element.
     */
    synchronized boolean add(SocketHandler handler) {
        return activeHandlersSet.add(handler);
    }

    /**
     * remove odebere z mnoiny aktivních handlerù nový handler.
     * Metoda je sychronizovaná, protoe HashSet neumí multithreading.
     * 
     * @param handler - reference na handler, který se má odstranit
     * @return true if the set did not already contain the specified element.
     */
    synchronized boolean remove(SocketHandler handler) {
        return activeHandlersSet.remove(handler);
    }
}
