package snowflake.components.files.browser;

import snowflake.common.FileInfo;
import snowflake.common.FileSystem;
import snowflake.common.ssh.SshUserInteraction;
import snowflake.components.files.FileComponentHolder;
import snowflake.components.files.browser.local.LocalFileBrowserView;
import snowflake.components.files.browser.ssh.SftpFileBrowserView;
import snowflake.components.newsession.SessionInfo;

import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileBrowser extends JPanel {
    private DefaultComboBoxModel<Object> leftList, rightList;
    private JSplitPane horizontalSplitter;
    private JComboBox<Object> leftDropdown, rightDropdown;
    private CardLayout leftCard, rightCard;
    private JPanel leftPanel, rightPanel;
    private AtomicBoolean closeRequested;
    private FileComponentHolder holder;
    private JRootPane rootPane;

    public FileBrowser(SessionInfo info,
                       SshUserInteraction source,
                       Map<SessionInfo, FileSystem> fileSystemMap,
                       Map<FileSystem, Integer> fileViewMap,
                       AtomicBoolean closeRequested,
                       FileComponentHolder holder,
                       JRootPane rootPane) {
        super(new BorderLayout());
        this.closeRequested = closeRequested;
        this.holder = holder;
        this.rootPane = rootPane;
        leftList = new DefaultComboBoxModel<>();
        leftList.addElement("New");
        rightList = new DefaultComboBoxModel<>();
        rightList.addElement("New");

        horizontalSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitter.setResizeWeight(0.5);

        leftCard = new CardLayout();
        rightCard = new CardLayout();

        leftPanel = new JPanel(leftCard);
        rightPanel = new JPanel(rightCard);

        leftDropdown = new JComboBox<>(leftList);
        rightDropdown = new JComboBox<>(rightList);

        leftDropdown.addItemListener(e -> {
            int index = leftDropdown.getSelectedIndex();
            if (index != -1) {
                Object obj = leftList.getElementAt(index);
                if (obj instanceof String) {

                } else {
                    leftCard.show(leftPanel, obj.hashCode() + "");
                }
            }
        });

        JPanel leftPanelHolder = new JPanel(new BorderLayout());
        JPanel rightPanelHolder = new JPanel(new BorderLayout());

        leftPanelHolder.add(leftDropdown, BorderLayout.NORTH);
        rightPanelHolder.add(rightDropdown, BorderLayout.NORTH);
        leftPanelHolder.add(leftPanel);
        rightPanelHolder.add(rightPanel);

        horizontalSplitter.setLeftComponent(leftPanelHolder);
        horizontalSplitter.setRightComponent(rightPanelHolder);
        horizontalSplitter.setDividerSize(2);

        add(horizontalSplitter);

        SftpFileBrowserView fv1 = new SftpFileBrowserView(this, rootPane, holder);
        leftList.addElement(fv1);
        leftDropdown.setSelectedIndex(1);
        leftPanel.add(fv1, fv1.hashCode() + "");
        leftCard.show(leftPanel, fv1.hashCode() + "");

        LocalFileBrowserView fv2 = new LocalFileBrowserView(this, rootPane, holder);
        rightList.addElement(fv2);
        rightDropdown.setSelectedIndex(1);
        rightPanel.add(fv2, fv2.hashCode() + "");
        rightCard.show(rightPanel, fv2.hashCode() + "");
    }

    public void close() {
        this.closeRequested.set(true);
    }

    public boolean isCloseRequested() {
        return closeRequested.get();
    }

    public void disableUi() {
        holder.disableUi();
    }

    public void enableUi() {
        holder.enableUi();
    }

    public void openSftpFileBrowserView(String path) {
        SftpFileBrowserView fv1 = new SftpFileBrowserView(this, rootPane, holder);
        leftList.addElement(fv1);
        leftDropdown.setSelectedIndex(1);
        leftPanel.add(fv1, fv1.hashCode() + "");
        leftCard.show(leftPanel, fv1.hashCode() + "");
    }

    public void openLocalFileBrowserView(String path) {
        LocalFileBrowserView fv2 = new LocalFileBrowserView(this, rootPane, holder);
        rightList.addElement(fv2);
        rightDropdown.setSelectedIndex(1);
        rightPanel.add(fv2, fv2.hashCode() + "");
        rightCard.show(rightPanel, fv2.hashCode() + "");
    }

    public void newFileTransfer(FileSystem sourceFs,
                                FileSystem targetFs,
                                FileInfo[] files,
                                String sourceFolder,
                                String targetFolder,
                                int dragsource) {
        holder.newFileTransfer(sourceFs, targetFs, files, sourceFolder, targetFolder, dragsource);
    }

    public void requestReload(int sourceHashcode) {
        for (int i = 0; i < this.leftList.getSize(); i++) {
            Object obj = leftList.getElementAt(i);
            if (obj.hashCode() == sourceHashcode) {
                ((AbstractFileBrowserView) obj).reload();
                return;
            }
        }
        for (int i = 0; i < this.rightList.getSize(); i++) {
            Object obj = rightList.getElementAt(i);
            if (obj.hashCode() == sourceHashcode) {
                ((AbstractFileBrowserView) obj).reload();
            }
        }
    }
}
