package com.subscriptionmonitor.gui.util;

import com.subscriptionmonitor.gui.exception.ApiException;

import javax.swing.*;
import java.awt.*;

public class ErrorDialogUtils {

    public static void showError(Component parent, Exception ex, String defaultTitle) {
        Throwable cause = ex.getCause();
        String message;
        String title = defaultTitle;

        if (cause instanceof ApiException) {
            ApiException apiEx = (ApiException) cause;
            message = apiEx.getUserFriendlyMessage();

            if (apiEx.hasErrorCode("VALIDATION_ERROR")) {
                title = "Ошибка валидации";
            } else if (apiEx.hasErrorCode("ACCESS_DENIED")) {
                title = "Доступ запрещен";
            } else if (apiEx.hasErrorCode("DUPLICATE_ENTRY")) {
                title = "Дубликат данных";
            } else if (apiEx.isClientError()) {
                title = "Ошибка запроса";
            } else if (apiEx.isServerError()) {
                title = "Ошибка сервера";
            }
        } else {
            message = ex.getMessage();
        }

        JOptionPane.showMessageDialog(parent,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showErrorWithPrefix(Component parent, Exception ex, String messagePrefix, String defaultTitle) {
        Throwable cause = ex.getCause();
        String message;
        String title = defaultTitle;

        if (cause instanceof ApiException) {
            ApiException apiEx = (ApiException) cause;
            message = messagePrefix + ": " + apiEx.getUserFriendlyMessage();

            if (apiEx.hasErrorCode("VALIDATION_ERROR")) {
                title = "Ошибка валидации";
            } else if (apiEx.hasErrorCode("ACCESS_DENIED")) {
                title = "Доступ запрещен";
            } else if (apiEx.hasErrorCode("DUPLICATE_ENTRY")) {
                title = "Дубликат данных";
            } else if (apiEx.isClientError()) {
                title = "Ошибка запроса";
            } else if (apiEx.isServerError()) {
                title = "Ошибка сервера";
            }
        } else {
            message = messagePrefix + ": " + ex.getMessage();
        }

        JOptionPane.showMessageDialog(parent,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE);
    }
}
