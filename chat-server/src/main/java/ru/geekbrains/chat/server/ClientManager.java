package ru.geekbrains.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

//Отправка сообщение между клиентами в методе broadcastMesage.
//Например если сообщение начинается с @"Имя"

public class ClientManager implements Runnable{
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;
    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        // Удаление клиента
        removeClient();
        try {
            if(bufferedReader != null){
                bufferedReader.close();
            }if(bufferedWriter != null){
                bufferedWriter.close();
            }if(socket != null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void removeClient(){
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат");
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
                break;
                /*
                if(messageFromClient != null){
                    broadcastMessage(messageFromClient);
                    break;
                }*/
            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void broadcastMessage(String message){

        String[] separatedMessage = message.split(": ");
        String[] words;


        Character charecter = separatedMessage[1].charAt(0);

        if(charecter.equals('@')){
            words = message.split(" ");

            String nameToMessage = words[1].replaceFirst("@", "");
            String messageToClient = words[0];

            for (int i = 2; i < words.length; i++) {
                messageToClient += words[i] + " ";
            }

            for(ClientManager client: clients){
                try {
                    if(!client.name.equals(name) && nameToMessage.equals(client.name)){
                        client.bufferedWriter.write(messageToClient);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                }catch (IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }else {
            for(ClientManager client: clients){
                try {
                    if(!client.name.equals(name)){
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                }catch (IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }

        /*
        for(ClientManager client: clients){
            try {
                if(!client.name.equals(name)){
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
        */
    }
}
