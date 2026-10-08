package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.client.gui.button.ModelInfoButton;
import com.elfmcys.yesstevemodel.client.upload.UploadManager;
import com.elfmcys.yesstevemodel.model.format.Type;
import com.elfmcys.yesstevemodel.network.message.HandleFile;
import com.elfmcys.yesstevemodel.network.message.RefreshModelManage;
import com.elfmcys.yesstevemodel.network.message.RequestServerModelInfo;
import com.elfmcys.yesstevemodel.network.message.UploadFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

public class ModelManageScreen extends Screen {
    private static final int MAX_COUNT = 13;
    private final List<RequestServerModelInfo.Info> customModels;
    private final List<RequestServerModelInfo.Info> authModels;
    private volatile String uploadError = null;
    private GuiTextField textField;
    private static boolean isCustomModels = true;
    private int index = -1;
    private Action action = Action.EMPTY;
    private static int page = 0;
    private int modelsCount = 0;
    private int x;
    private int y;

    public ModelManageScreen(List<RequestServerModelInfo.Info> customModels, List<RequestServerModelInfo.Info> authModels) {
        this.customModels = customModels;
        this.authModels = authModels;
    }

    private void calculateList() {
        this.modelsCount = this.getModels().size();
        if ((this.modelsCount - 1) / MAX_COUNT < page) {
            page = 0;
        }
    }

    @Override
    public void initGui() {
        this.calculateList();
        this.x = (this.width - 420) / 2;
        this.y = (this.height - 235) / 2;
        this.addTopButtons();
        this.addPageButtons();
        this.addModelInfoButtons();
        if (this.index >= 0) {
            this.addActionButtons();
        }
        if (this.action != Action.EMPTY) {
            this.addExtraButtons();
        }
    }

    private void addExtraButtons() {
        if (this.action != Action.UPLOAD || StringUtils.isNoneBlank(UploadManager.FILE_PATH)) {
            this.addButton(new FlatColorButton(this.x + 270, this.y + 235 - 23, 70, 18, I18n.format("gui.yes_steve_model.model_manage.confirm"), (b) -> {
                boolean canConfirm = false;
                if (this.index >= 0 && this.index < this.getModels().size()) {
                    RequestServerModelInfo.Info info = this.getModels().get(this.index);
                    UploadFile.Dir dir = isCustomModels ? UploadFile.Dir.CUSTOM : UploadFile.Dir.AUTH;
                    if (this.action == Action.DELETE) {
                        com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new HandleFile(info.getFileName(), dir, "delete", ""));
                        canConfirm = true;
                    }
                    if (this.action == Action.MOVE) {
                        com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new HandleFile(info.getFileName(), dir, "move", ""));
                        canConfirm = true;
                    }
                    if (this.action == Action.RENAME && StringUtils.isNotBlank(this.textField.getText())) {
                        String value = this.textField.getText();
                        String fileName = info.getFileName();
                        if (info.getType() == Type.FOLDER && !value.equals(fileName)) {
                            com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new HandleFile(info.getFileName(), dir, "rename", value));
                            canConfirm = true;
                        }
                        if (info.getType() != Type.FOLDER && !value.equals(fileName.substring(0, fileName.length() - 4))) {
                            value = value + fileName.substring(fileName.length() - 4);
                            com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new HandleFile(info.getFileName(), dir, "rename", value));
                            canConfirm = true;
                        }
                    }
                }
                if (this.action == Action.UPLOAD && StringUtils.isNoneBlank(UploadManager.FILE_PATH) && UploadManager.STATUE == UploadManager.Statue.FULFILL) {
                    try {
                        this.uploadFile(UploadManager.FILE_PATH, isCustomModels);
                        canConfirm = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (canConfirm) {
                    this.action = Action.EMPTY;
                    com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new RefreshModelManage());
                }
            }));
            this.addButton(new FlatColorButton(this.x + 345, this.y + 235 - 23, 70, 18, I18n.format("gui.yes_steve_model.model_manage.cancel"), (b) -> {
                this.action = Action.EMPTY;
                this.refreshGui();
            }));
        }
        if (this.action == Action.RENAME) {
            this.textField = new GuiTextField(0, this.fontRenderer, this.x + 270, this.y + 51, 145, 14);
            this.textField.setTextColor(0xF3EFE0);
            this.textField.setMaxStringLength(24);
            this.textField.setCursorPositionEnd();
        }
    }

    private void addActionButtons() {
        this.addButton(new FlatColorButton(this.x + 5, this.y + 235 - 23, 80, 18, I18n.format("gui.yes_steve_model.model_manage.delete"), (b) -> {
            this.action = Action.DELETE;
            this.refreshGui();
        }));
        this.addButton(new FlatColorButton(this.x + 90, this.y + 235 - 23, 80, 18, I18n.format("gui.yes_steve_model.model_manage.move"), (b) -> {
            this.action = Action.MOVE;
            this.refreshGui();
        }));
        this.addButton(new FlatColorButton(this.x + 175, this.y + 235 - 23, 80, 18, I18n.format("gui.yes_steve_model.model_manage.rename"), (b) -> {
            this.action = Action.RENAME;
            this.refreshGui();
        }));
    }

    private void addModelInfoButtons() {
        int modelsY = this.y + 51;
        int count = page * MAX_COUNT;
        for (int i = count; i < count + MAX_COUNT; i++) {
            if (i >= this.getModels().size()) {
                return;
            }
            RequestServerModelInfo.Info info = this.getModels().get(i);
            final int finalIndex = i;
            ModelInfoButton modelInfoButton = new ModelInfoButton(this.x + 5, modelsY, 12, info, (b) -> {
                this.index = finalIndex;
                this.action = Action.EMPTY;
                this.refreshGui();
            });
            if (this.index == i) {
                modelInfoButton.setSelect(true);
            }
            this.addButton(modelInfoButton);
            modelsY += 12;
        }
    }

    private void addPageButtons() {
        this.addButton(new FlatColorButton(this.x + 5, this.y + 28, 80, 18, "<", (b) -> {
            if (page > 0) {
                page--;
                this.refreshGui();
            }
        }));
        this.addButton(new FlatColorButton(this.x + 260 - 85, this.y + 28, 80, 18, ">", (b) -> {
            if ((page + 1) * MAX_COUNT < this.modelsCount) {
                page++;
                this.refreshGui();
            }
        }));
    }

    private void addTopButtons() {
        FlatColorButton customButton = new FlatColorButton(this.x + 5, this.y + 5, 80, 18, I18n.format("gui.yes_steve_model.model_manage.custom"), (b) -> {
            if (!isCustomModels) {
                isCustomModels = true;
                this.index = -1;
                page = 0;
                this.action = Action.EMPTY;
                this.refreshGui();
            }
        });
        customButton.setSelect(isCustomModels);
        this.addButton(customButton);

        FlatColorButton authButton = new FlatColorButton(this.x + 90, this.y + 5, 80, 18, I18n.format("gui.yes_steve_model.model_manage.auth"), (b) -> {
            if (isCustomModels) {
                isCustomModels = false;
                this.index = -1;
                page = 0;
                this.action = Action.EMPTY;
                this.refreshGui();
            }
        });
        authButton.setSelect(!isCustomModels);
        this.addButton(authButton);

        this.addButton(new FlatColorButton(this.x + 175, this.y + 5, 80, 18, I18n.format("gui.yes_steve_model.model_manage.upload"), (b) -> {
            if (UploadManager.STATUE == UploadManager.Statue.FULFILL) {
                UploadManager.FILE_PATH = "";
            }
            this.action = Action.UPLOAD;
            this.refreshGui();
            if (UploadManager.STATUE == UploadManager.Statue.FULFILL) {
                new Thread(this::getUploadFilePath).start();
            }
        }));
    }

    private void getUploadFilePath() {
        FileDialog dialog = new FileDialog((Frame) null, I18n.format("gui.yes_steve_model.model_manage.open_file"), FileDialog.LOAD);
        dialog.setMultipleMode(false);
        dialog.setVisible(true);

        String directory = dialog.getDirectory();
        String filename = dialog.getFile();
        if (StringUtils.isBlank(directory) || StringUtils.isBlank(filename)) {
            return;
        }
        File file = new File(directory, filename);
        if (file.isFile()) {
            this.uploadError = null;
            if (!file.getName().endsWith("zip") && !file.getName().endsWith("ysm")) {
                this.uploadError = I18n.format("gui.yes_steve_model.model_manage.error.format_incorrect");
                return;
            }
            if (FileUtils.sizeOf(file) > 32000) {
                this.uploadError = I18n.format("gui.yes_steve_model.model_manage.error.too_large");
                return;
            }
            UploadManager.FILE_PATH = file.getAbsolutePath();
            Minecraft.getMinecraft().addScheduledTask(this::refreshGui);
        }
    }

    private void uploadFile(String filePath, boolean isCustom) throws IOException {
        File file = Paths.get(filePath).toFile();
        if (file.isFile()) {
            String name = file.getName();
            byte[] bytes = FileUtils.readFileToByteArray(file);
            UploadFile.Dir dir = isCustom ? UploadFile.Dir.CUSTOM : UploadFile.Dir.AUTH;
            com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new UploadFile(name, bytes, dir));
            UploadManager.STATUE = UploadManager.Statue.PROCESSING;
        }
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTick) {
        this.drawDefaultBackground();
        this.drawGradientRect(this.x, this.y, this.x + 260, this.y + 235, 0xff_222222, 0xff_222222);
        this.drawGradientRect(this.x + 265, this.y, this.x + 420, this.y + 235, 0xff_222222, 0xff_222222);
        this.drawGradientRect(this.x + 270, this.y + 5, this.x + 415, this.y + 23, 0xff_434242, 0xff_434242);

        this.drawCenteredString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.action_info"), this.x + 342, this.y + 11, 0xFFFFFF);
        this.drawString(this.fontRenderer, String.format("%d/%d", page + 1, (this.modelsCount - 1) / MAX_COUNT + 1), this.x + 120, this.y + 33, 0xFFFFFF);

        if (this.action == Action.UPLOAD) {
            String folder = isCustomModels ? I18n.format("gui.yes_steve_model.model_manage.custom") : I18n.format("gui.yes_steve_model.model_manage.auth");
            String actionName = I18n.format("gui.yes_steve_model.model_manage." + this.action.name().toLowerCase(Locale.US));
            this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.selected", TextFormatting.RESET + folder), this.x + 272, this.y + 29, 0xFFFFFF);
            this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.action", TextFormatting.RESET + actionName), this.x + 272, this.y + 39, 0xFFFFFF);
            String uploadStatue = I18n.format("gui.yes_steve_model.model_manage.upload.statue." + UploadManager.STATUE.name().toLowerCase(Locale.US));
            this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.upload.statue", TextFormatting.RESET + uploadStatue), this.x + 272, this.y + 49, 0xFFFFFF);
            String fileUpload = I18n.format("gui.yes_steve_model.model_manage.file.empty");
            if (StringUtils.isNoneBlank(UploadManager.FILE_PATH)) {
                fileUpload = UploadManager.FILE_PATH;
            }
            int yOffset = this.drawWordWrap(I18n.format("gui.yes_steve_model.model_manage.file", TextFormatting.RESET + fileUpload), this.x + 272, this.y + 59, 145, 0xFFFFFF);
            if (this.uploadError != null) {
                this.drawWordWrap(this.uploadError, this.x + 272, this.y + 60 + yOffset, 145, 0xFFFFFF);
            }
        }

        if (this.index >= 0 && this.index < this.getModels().size() && this.action != Action.UPLOAD) {
            RequestServerModelInfo.Info info = this.getModels().get(this.index);
            this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.selected", TextFormatting.RESET + info.getFileName()), this.x + 272, this.y + 29, 0xFFFFFF);
            if (this.action != Action.EMPTY) {
                String actionName = I18n.format("gui.yes_steve_model.model_manage." + this.action.name().toLowerCase(Locale.US));
                this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.action", TextFormatting.RESET + actionName), this.x + 272, this.y + 39, 0xFFFFFF);
            }
            if (this.action == Action.RENAME && this.textField != null) {
                this.textField.drawTextBox();
            }
        }
        super.drawScreen(pMouseX, pMouseY, pPartialTick);
    }

    private List<RequestServerModelInfo.Info> getModels() {
        if (isCustomModels) {
            return this.customModels;
        }
        return this.authModels;
    }

    @Override
    public void keyTyped(char codePoint, int modifiers) throws IOException {
        if (this.textField != null && this.textField.textboxKeyTyped(codePoint, modifiers)) return;
        super.keyTyped(codePoint, modifiers);
    }

    @Override
    public void onResize(@Nonnull Minecraft minecraft, int width, int height) {
        super.onResize(minecraft, width, height);
        if (this.textField != null) {
            String value = this.textField.getText();
            super.onResize(minecraft, width, height);
            this.textField.setText(value);
        }
    }

    @Override
    public void updateScreen() {
        if (this.textField != null) {
            this.textField.updateCursorCounter();
        }
    }

    public enum Action {
        DELETE, MOVE, RENAME, UPLOAD, EMPTY
    }
}
