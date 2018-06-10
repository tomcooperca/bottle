package com.github.tomcooperca.bottle;

import com.github.tomcooperca.bottle.repository.Message;
import com.google.common.collect.EvictingQueue;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringUI
@RequiredArgsConstructor
@Slf4j
public class IndexUI extends UI {

    private final MessageService messageService;
    private TextArea messagesTextArea = new TextArea("What others are saying...");
    private Panel mainPanel = new Panel("Messages in a bottle");
    private Button send = new Button("Send a message", VaadinIcons.PENCIL);
    private TextField messageField = new TextField();
    private EvictingQueue<String> messageQueue = EvictingQueue.create(3);
    private int pollInterval = 5000;

    @Override
    protected void init(VaadinRequest request) {
        pollInterval = messageService.calculatePoll(messageQueue.peek());
        setPollInterval(pollInterval);
        addPollListener(e -> {
                displayMessage(request);
                send.setIcon(VaadinIcons.PENCIL);
                send.setCaption("Send a message");
        });
        // Parent layout
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        // Message panel
        // Inner layout
        VerticalLayout messageLayout = new VerticalLayout();

        // Form panel
        FormLayout formLayout = new FormLayout();
        HorizontalLayout formHorizontal = new HorizontalLayout();
        formHorizontal.setSizeFull();
        messageField.setWidth("75%");
        messageField.setPlaceholder("Enter a message...");
        messageField.addShortcutListener(new ShortcutListener("Enter key shortcut", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                saveMessage(request);
            }
        });

        send.addClickListener(e -> saveMessage(request));
        formHorizontal.addComponents(messageField, send);
        formLayout.addComponent(formHorizontal);

        messageLayout.addComponents(messagesTextArea, formLayout);
        mainPanel.setContent(messageLayout);
        mainLayout.addComponent(mainPanel);
        setContent(mainLayout);

    }

    private void saveMessage(VaadinRequest request) {
        if (!messageField.isEmpty()) {
            messageService.saveMessage(messageField.getValue(), request.getRemoteAddr());
            send.setIcon(VaadinIcons.CHECK);
            send.setCaption("Sent!");
            messageField.clear();
        }
    }

    private void displayMessage(VaadinRequest request) {
        int retry = 0;
        Message message = messageService.randomMessageEntity();
        while (message.getOriginator().equals(request.getRemoteAddr()) &&
                !messagesTextArea.getValue().equals(message.getContent())) {
            log.debug("Attempt to display a message that was posted by this user");
            message = messageService.randomMessageEntity();
            retry += 1;
            if (retry > 3) return;
        }
        log.debug("Adding message {} to queue", message.getUuid());
        messageQueue.add(message.getContent());
        List<String> list = new ArrayList<>(messageQueue);
        Collections.reverse(list);
        messagesTextArea.setValue(String.join("\n\n", list));
    }
}
