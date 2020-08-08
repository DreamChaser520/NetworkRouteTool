package com.zh.ui;

import com.zh.bean.IPInfo;
import com.zh.bean.Interface;
import com.zh.utils.CMDUtil;
import com.zh.utils.ConfigUtil;
import com.zh.utils.InterfaceUtil;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//继承JFrame顶层容器类
public class MainUI extends JFrame {
    private static final Logger logger= Logger.getLogger(MainUI.class);
    //定义组件
    //定义面板
    JPanel panelResult, panelName, panelInterface, panelButton, panelWhiteList;
    //定义标签
    JLabel labelInside, labelOutside, labelWhiteList;
    //定义下拉框
    JComboBox comboBoxInside, comboBoxOutside;
    //定义按钮
    JButton buttonExe, buttonRenew;
    //分割
    JSplitPane splitPaneResult, splitPaneWhiteList, splitPaneInfo;
    JTextArea textAreaWhiteList, textAreaResult;
    JScrollPane scrollPaneWhiteList, scrollPaneResult;
    int insideIndex;
    int outsideIndex;
    String insideGateway, outsideGateway, insideNetmask, insideNetSegment;
    //是否查询到网络接口
    boolean flag = false;
    List<Interface> interfaceList;
    String[] inside;
    String[] outside;
    List<Interface> interfaces = new ArrayList<>();


    public MainUI() {
        ItemListener insideListener = arg0 -> {
            if (flag) {
                if (ItemEvent.SELECTED == arg0.getStateChange()) {
                    String selectedItem = arg0.getItem().toString();
                    insideIndex = InterfaceUtil.getIndex(selectedItem);
                    for (Interface anInterface : interfaceList) {
                        if (anInterface.getIndex() == insideIndex)
                            insideGateway = anInterface.getGateway();
                    }
                    int selectIndex = InterfaceUtil.getIndex(selectedItem);
                    for (Interface inf : interfaceList) {
                        if (inf.getIndex() == selectIndex) {
                            insideNetmask = inf.getNetmask();
                            insideNetSegment = InterfaceUtil.getNetSegment(inf.getIp(), insideNetmask);
                        }
                    }
                }
            }
        };
        ItemListener outsideListener = arg1 -> {
            if (flag) {
                if (ItemEvent.SELECTED == arg1.getStateChange()) {
                    String selectedItem = arg1.getItem().toString();
                    outsideIndex = InterfaceUtil.getIndex(selectedItem);
                    for (Interface anInterface : interfaceList) {
                        if (anInterface.getIndex() == outsideIndex)
                            outsideGateway = anInterface.getGateway();
                    }
                }
            }
        };

        ActionListener exeActionListener = arg2 -> {
            // 进行逻辑处理即可
            String result0 = "";
            if (flag) {
                textAreaResult.setDisabledTextColor(Color.RED);
                if (insideGateway.equals("0.0.0.0")) {
                    textAreaResult.setText("未获取到内网网关信息，无法执行，请尝试刷新，如还结果还是如此，请检查网卡或网线是否正常。");
                } else if (outsideGateway.equals("0.0.0.0")) {
                    textAreaResult.setText("未获取到外网网关信息，无法执行，请尝试刷新，如还结果还是如此，请检查网卡或网线是否正常。");
                } else if (interfaceList.size() < 2) {
                    textAreaResult.setText("当前只检测到有一张网卡，无法执行，请尝试刷新，如还结果还是如此，请检查网卡或网线是否正常。");
                } else if (insideIndex == outsideIndex) {
                    textAreaResult.setText("内外网为同一张网卡，无法执行，请检查网卡配置。");
                } else if (insideNetSegment.equals("0.0.0.0")) {
                    textAreaResult.setText("无法获取到内网网段，请尝试刷新，如还结果还是如此，请检查网卡配置。");
                } else {
                    textAreaResult.setDisabledTextColor(Color.BLACK);
                    String cmd1 = "route delete 0.0.0.0 mask 0.0.0.0 " + insideGateway;
                    String cmd2 = "route add " + insideNetSegment + " mask " + insideNetmask + " " + insideGateway + " if " + insideIndex + " metric " + 10;
                    String cmd3 = "route change 0.0.0.0 mask 0.0.0.0 " + outsideGateway + " if " + outsideIndex + " metric " + 30;

                    String result1 = CMDUtil.ExeCMD(cmd1);
                    String result2 = CMDUtil.ExeCMD(cmd2);
                    String result3 = CMDUtil.ExeCMD(cmd3);
                    textAreaResult.setDisabledTextColor(Color.BLACK);
                    textAreaResult.setText("删除内网默认路由：" + result1.trim() + "\n添加内网路由：" + result2.trim() + "\n改变外网默认路由：" + result3.trim());

                    String whiteListText = textAreaWhiteList.getText();
                    //白名单写入成功标记
                    Boolean whiteListSuccessFlag =false;
                    if (whiteListText.length() != 0) {
                        List<String> results = new ArrayList<>();
                        try {
                            //获取白名单列表
                            List<IPInfo> ipInfoList = InterfaceUtil.getIPInfo(whiteListText);
                            for (int i = 0; i < ipInfoList.size(); i++) {
                                String ip = ipInfoList.get(i).getIp();
                                String netmask = ipInfoList.get(i).getNetmask();
                                //白名单格式没有问题则写入白名单
                                if (InterfaceUtil.isIP(ip) && InterfaceUtil.isIP(netmask)) {
                                    ConfigUtil.writeWhiteList(whiteListText);
                                    results.add(CMDUtil.ExeCMD("route add " + ip + " mask " + netmask + " " + insideGateway + " if " + insideIndex + " metric " + 20));
                                    whiteListSuccessFlag=true;
                                } else {
                                    textAreaResult.setDisabledTextColor(Color.RED);
                                    result0 = "白名单中第" + (i + 1) + "条记录中IP或子网掩码格式错误，请确保每个字段都在0~255之间。\n示例：“192.168.1.0 255.255.255.0”。";
                                    break;
                                }
                            }

                        } catch (Exception e) {
                            logger.error("白名单格式错误",e);
                            textAreaResult.setDisabledTextColor(Color.RED);
                            result0 = "白名单格式错误！IP和子网掩码以空格隔开，多条名单内容则以回车隔开。\n示例：“192.168.1.0 255.255.255.0”。";
                        }

                        if (!result0.equals("")) {
                            textAreaResult.append("\n添加白名单路由：" + result0.trim());
                        } else {
                            for (int i = 0; i < results.size(); i++) {
                                logger.info("\n添加第" + (i + 1) + "条白名单路由：" + results.get(i).trim());
                                textAreaResult.append("\n添加第" + (i + 1) + "条白名单路由：" + results.get(i).trim());
                            }
                            if(whiteListSuccessFlag)
                            textAreaResult.append("更新白名单成功");
                        }
                    }
                }
            } else {
                textAreaResult.setDisabledTextColor(Color.RED);
                logger.warn("未查询到网卡信息，无法执行！");
                textAreaResult.setText("未查询到网卡信息，无法执行！");
            }
        };

        ActionListener renewActionListener = arg4 -> {
            if (insideGateway.equals("0.0.0.0") || outsideGateway.equals("0.0.0.0")) {
                textAreaResult.setText("正在重新获取ip信息并重新加载网卡，可能需要耗费数秒，请稍等。。。");
                CMDUtil.ExeCMD("ipconfig /renew");
            }
            init();
        };

        panelName = new JPanel();
        panelInterface = new JPanel();
        //创建内外网条目容器
        comboBoxInside = new JComboBox<>();
        comboBoxOutside = new JComboBox<>();
        //创建标签
        labelInside = new JLabel("内网网卡：");
        labelOutside = new JLabel("外网网卡：");

        //添加的组件
        panelName.add(labelInside);
        panelInterface.add(comboBoxInside);

        comboBoxInside.addItemListener(insideListener);
        comboBoxOutside.addItemListener(outsideListener);

        comboBoxInside.setRenderer(new CustomComboBoxRenderer());
        comboBoxOutside.setRenderer(new CustomComboBoxRenderer());

        panelName.setLayout(new GridLayout(2, 1, 10, 10));
        panelInterface.setLayout(new GridLayout(2, 1, 10, 10));

        panelName.add(labelOutside);
        panelInterface.add(comboBoxOutside);
        //创建拆分窗格
        splitPaneInfo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelName, panelInterface);
        splitPaneInfo.setDividerLocation(100);
        splitPaneInfo.setEnabled(false);
        splitPaneInfo.setBorder(new EmptyBorder(5, 10, 5, 10));

        panelWhiteList = new JPanel();
        labelWhiteList = new JLabel("内网白名单：");
        textAreaWhiteList = new JTextArea();
        scrollPaneWhiteList = new JScrollPane();
        //自动换行
        textAreaWhiteList.setLineWrap(true);
        //从第一个位置开始
        textAreaWhiteList.setCaretPosition(0);
        //创建滚动窗格
        scrollPaneWhiteList = new JScrollPane(textAreaWhiteList);

        splitPaneWhiteList = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, labelWhiteList, scrollPaneWhiteList);
        splitPaneWhiteList.setDividerLocation(100);
        splitPaneWhiteList.setEnabled(false);
        splitPaneWhiteList.setBorder(new EmptyBorder(5, 10, 5, 10));

        panelResult = new JPanel();
        panelButton = new JPanel();

        textAreaResult = new JTextArea();

        buttonRenew = new JButton("刷新");
        buttonExe = new JButton("执行");

        buttonRenew.addActionListener(renewActionListener);
        buttonExe.addActionListener(exeActionListener);

        panelButton.setLayout(new GridLayout(2, 1, 5, 5));
        panelButton.setBorder(new EmptyBorder(5, 10, 5, 0));
        panelButton.add(buttonRenew);
        panelButton.add(buttonExe);

        textAreaResult.setEnabled(false);
        //自动换行
        textAreaResult.setLineWrap(true);
        //从第一个位置开始
        textAreaResult.setCaretPosition(0);
        //创建滚动窗格
        scrollPaneResult = new JScrollPane(textAreaResult);
        //创建拆分窗格
        splitPaneResult = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneResult, panelButton);
        splitPaneResult.setDividerLocation(570);
        splitPaneResult.setEnabled(false);
        splitPaneResult.setBorder(new EmptyBorder(5, 10, 5, 10));
        panelResult.add(splitPaneResult);


        //设置布局管理器
        //设置网格布局，2行1列
        this.setLayout(new GridLayout(3, 1));

        //加入组件
        this.add(splitPaneInfo);
        this.add(splitPaneWhiteList);
        this.add(splitPaneResult);

        //设置窗体属性
        //设置界面标题
        this.setTitle("Network Route Tool v1.3 by 周航");
        //设置界面像素
        this.setSize(700, 250);
        //窗口居中
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        //设置虚拟机和界面一同关闭
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置界面可视化
        this.setVisible(true);

        init();
    }


    public void init() {
        textAreaResult.setDisabledTextColor(Color.BLACK);
        this.textAreaResult.setText("加载网卡信息中...");
        //接口列表
        interfaceList = InterfaceInfo.getInterfaces(interfaces);
        if (interfaceList != null) {
            int size = interfaceList.size();

            if (size == 0) {
                flag = false;
                inside = new String[1];
                outside = new String[1];
                inside[0] = "没有查询到网络接口";
                outside[0] = "没有查询到网络接口";
            } else {
                //查询到网络接口
                flag = true;
                inside = new String[size];
                outside = new String[size];
                for (int i = 0; i < interfaceList.size(); i++) {
                    //准备加载到GUI的数组
                    inside[i] = interfaceList.get(i).toString();
                    outside[i] = interfaceList.get(i).toString();
                }
                //不作任何选择时的初始值
                insideIndex = InterfaceUtil.getIndex(inside[0]);
                outsideIndex = InterfaceUtil.getIndex(outside[0]);

                insideGateway = interfaceList.get(0).getGateway();
                outsideGateway = interfaceList.get(0).getGateway();

                insideNetmask = interfaceList.get(0).getNetmask();
                insideNetSegment = InterfaceUtil.getNetSegment(interfaceList.get(0).getIp(), insideNetmask);
            }
            if (this.comboBoxInside != null) {
                this.comboBoxInside.removeAllItems();
            }
            if (this.comboBoxOutside != null) {
                this.comboBoxOutside.removeAllItems();
            }
            for (int i = 0; i < inside.length; i++) {
                this.comboBoxInside.addItem(inside[i]);
                this.comboBoxOutside.addItem(outside[i]);
            }
            textAreaResult.setDisabledTextColor(Color.BLACK);
            this.textAreaResult.setText("加载网卡信息成功。");
        } else {
            textAreaResult.setDisabledTextColor(Color.RED);
            this.textAreaResult.setText("加载网卡信息失败。");
        }
        try {
            String whiteList = ConfigUtil.readWhiteList();
            if (whiteList.length() != 0) {
                if (whiteList.equals("error")) {
                    textAreaResult.setDisabledTextColor(Color.ORANGE);
                    this.textAreaResult.append("\n未找到白名单配置，请创建" + new File(".").getCanonicalPath() + "\\WhiteList.config文件，如不需要白名单请忽略。");
                } else {
                    textAreaWhiteList.setText(whiteList);
                    this.textAreaResult.append("\n加载白名单配置信息成功。");
                }
            }
        } catch (IOException e) {
            logger.error("加载白名单配置信息失败", e);
            textAreaResult.setDisabledTextColor(Color.RED);
            this.textAreaResult.append("\n加载白名单配置信息失败。");
        }
    }

    static class CustomComboBoxRenderer extends BasicComboBoxRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                if (-1 < index) {
                    list.setToolTipText((value == null) ? null : value.toString());
                }
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}
