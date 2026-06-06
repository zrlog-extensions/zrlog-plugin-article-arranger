import React, { useState, useEffect } from "react";
import { 
  Form, 
  Input, 
  Button, 
  Card, 
  Space, 
  Tooltip,
  Typography,
  Flex,
  Row,
  Col,
  Grid,
  theme,
  App,
  Checkbox,
  Alert
} from "antd";
import { 
  OrderedListOutlined, 
  SettingOutlined, 
  ReloadOutlined, 
  BgColorsOutlined,
  CodeOutlined,
  CheckCircleOutlined,
  BookOutlined,
  CheckSquareOutlined,
  BorderOutlined
} from "@ant-design/icons";
import axios from "axios";
import { ArrangerInfoResponse, StandardResponse, CategoryGroup } from "./index";

const { Title, Text, Paragraph } = Typography;

interface AppBaseProps {
  data: ArrangerInfoResponse;
  setResponse: React.Dispatch<React.SetStateAction<StandardResponse<ArrangerInfoResponse> | null>>;
}

const PRESET_COLORS = [
  { color: "#007BFF", label: "亮蓝" },
  { color: "#1677ff", label: "标准蓝" },
  { color: "#2f54eb", label: "深蓝" },
  { color: "#52c41a", label: "绿色" },
  { color: "#faad14", label: "黄色" },
  { color: "#f5222d", label: "红色" },
  { color: "#722ed1", label: "紫色" },
  { color: "#13c2c2", label: "青色" }
];

const AppBase: React.FC<AppBaseProps> = ({ data, setResponse }) => {
  const { token } = theme.useToken();
  const screens = Grid.useBreakpoint();
  const isPhone = Boolean(screens.xs && !screens.sm);
  const isCompact = !screens.lg;
  const { message } = App.useApp();

  const [loading, setLoading] = useState<boolean>(false);
  const [saveLoading, setSaveLoading] = useState<boolean>(false);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [form] = Form.useForm();
  const mainColor = Form.useWatch("mainColor", form);

  // Initialize selected article IDs
  useEffect(() => {
    if (data && data.config && data.config.groups) {
      const ids: number[] = [];
      data.config.groups.forEach((group) => {
        if (group.items) {
          group.items.forEach((item) => {
            if (item.arrange_plugin === data.plugin.shortName) {
              ids.push(item.id);
            }
          });
        }
      });
      setSelectedIds(ids);
    }
  }, [data]);

  // Reload page data
  const refreshPage = async (silent = false) => {
    setLoading(true);
    try {
      const { data: res } = await axios.get<StandardResponse<ArrangerInfoResponse>>("json");
      if (res.success) {
        setResponse(res);
        if (!silent) {
          message.success("数据刷新成功");
        }
      } else {
        message.error(res.message || "获取配置失败");
      }
    } catch (e) {
      console.error(e);
      message.error("获取配置失败");
    } finally {
      setLoading(false);
    }
  };

  // Toggle selection of an article
  const toggleArticle = (id: number) => {
    setSelectedIds((prev) => 
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  // Toggle all articles in a category
  const toggleCategory = (group: CategoryGroup) => {
    const groupItemIds = group.items.map(item => item.id);
    const allSelected = groupItemIds.every(id => selectedIds.includes(id));

    if (allSelected) {
      // Deselect all in this category
      setSelectedIds(prev => prev.filter(id => !groupItemIds.includes(id)));
    } else {
      // Select all in this category
      setSelectedIds(prev => {
        const otherIds = prev.filter(id => !groupItemIds.includes(id));
        return [...otherIds, ...groupItemIds];
      });
    }
  };

  // Save configurations
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaveLoading(true);

      const params = new URLSearchParams();
      params.append("styleGlobal", values.styleGlobal || "");
      params.append("mainColor", values.mainColor || "#007BFF");
      
      // Append all checked articles
      selectedIds.forEach((id) => {
        params.append("item", String(id));
      });

      const { data: res } = await axios.post<StandardResponse<any>>("update", params, {
        headers: { "Content-Type": "application/x-www-form-urlencoded" }
      });

      if (res && res.success) {
        message.success("排版设置保存成功");
        // Reload data to reflect changes
        setTimeout(() => refreshPage(true), 500);
      } else {
        message.error(res.message || "排版设置保存失败");
      }
    } catch (e) {
      console.error(e);
      message.error("请检查输入内容是否有误");
    } finally {
      setSaveLoading(false);
    }
  };

  const selectColorPreset = (color: string) => {
    form.setFieldsValue({ mainColor: color });
  };

  return (
    <div
      style={{
        width: "100%",
        maxWidth: 1240,
        margin: "0 auto",
        padding: isPhone ? 12 : isCompact ? 16 : 24,
        boxSizing: "border-box",
      }}
    >
      {/* Upper Navigation & Hero */}
      <Flex
        justify="space-between"
        align="flex-start"
        gap={16}
        vertical={isCompact}
        style={{ marginBottom: 20 }}
      >
        <div>
          <Flex align="center" gap={10}>
            <div 
              style={{ 
                background: `linear-gradient(135deg, ${token.colorPrimary} 0%, ${token.colorPrimaryActive} 100%)`, 
                borderRadius: 8, 
                padding: "8px 10px", 
                color: "#fff",
                boxShadow: `0 4px 10px rgba(0, 123, 255, 0.2)`
              }}
            >
              <OrderedListOutlined style={{ fontSize: 24, display: "flex" }} />
            </div>
            <div>
              <Title level={2} style={{ margin: 0, fontSize: isPhone ? 20 : 24, fontWeight: 700, letterSpacing: 0 }}>
                {data.plugin.name}
              </Title>
            </div>
          </Flex>
        </div>
        <Space wrap style={{ width: isPhone ? "100%" : undefined }}>
          <Button icon={<ReloadOutlined/>} onClick={() => refreshPage(false)} loading={loading} style={isPhone ? {flex: 1} : undefined}>
            刷新数据
          </Button>
          <Button 
            type="primary" 
            icon={<CheckCircleOutlined/>} 
            onClick={handleSave} 
            loading={saveLoading}
            style={isPhone ? {flex: 1} : { minWidth: 100 }}
          >
            保存配置
          </Button>
        </Space>
      </Flex>

      {/* Description Info */}
      <Alert
        message={<Text style={{ fontWeight: 600 }}>文章聚合说明</Text>}
        description={
          <Paragraph style={{ margin: 0, fontSize: 13 }}>
            勾选需要聚合的文章后，插件会在前台生成带目录的文章列表页，用于系列文章、文档页等需要按顺序浏览的内容。
          </Paragraph>
        }
        type="info"
        showIcon
        icon={<BookOutlined />}
        style={{ marginBottom: 20, borderRadius: token.borderRadiusLG }}
      />

      <Form
        form={form}
        layout="vertical"
        initialValues={{
          styleGlobal: data.config.styleGlobal,
          mainColor: data.config.mainColor
        }}
      >
        <Row gutter={[isCompact ? 16 : 20, isCompact ? 16 : 20]}>
          {/* Left Column: Style Config */}
          <Col xs={24} lg={10}>
            <Space direction="vertical" size={20} style={{ width: "100%" }}>
              {/* Main Color Picker Card */}
              <Card
                title={
                  <Flex align="center" gap={8}>
                    <BgColorsOutlined style={{ color: token.colorPrimary }} />
                    <span style={{ fontWeight: 600 }}>前台主题颜色</span>
                  </Flex>
                }
                bordered
                style={{ 
                  borderRadius: token.borderRadiusLG,
                  borderColor: token.colorBorderSecondary,
                  boxShadow: "0 2px 8px rgba(0,0,0,0.02)"
                }}
              >
                <Form.Item
                  name="mainColor"
                  tooltip="用于前台聚合页的激活文字、侧边栏激活条和控件主色。"
                  rules={[{ required: true, message: "主题颜色不能为空" }]}
                >
                  <Input 
                    placeholder="请输入十六进制颜色，例如 #007BFF" 
                    addonAfter={
                      <div 
                        style={{ 
                          width: 18, 
                          height: 18, 
                          borderRadius: 4, 
                          backgroundColor: mainColor || "#007BFF",
                          border: "1px solid rgba(0,0,0,0.1)",
                          transition: "background-color 0.3s"
                        }} 
                      />
                    }
                  />
                </Form.Item>

                <Text type="secondary" style={{ display: "block", marginBottom: 10, fontSize: 12 }}>
                  快捷色彩预设：
                </Text>
                <Flex gap={8} wrap="wrap">
                  {PRESET_COLORS.map((preset) => (
                    <Tooltip title={preset.label} key={preset.color}>
                      <div
                        onClick={() => selectColorPreset(preset.color)}
                        style={{
                          width: 24,
                          height: 24,
                          borderRadius: 6,
                          backgroundColor: preset.color,
                          cursor: "pointer",
                          border: mainColor === preset.color 
                            ? `2px solid ${token.colorText}` 
                            : "2px solid transparent",
                          boxSizing: "border-box",
                          transition: "all 0.2s",
                          transform: mainColor === preset.color ? "scale(1.1)" : "none"
                        }}
                      />
                    </Tooltip>
                  ))}
                </Flex>
              </Card>

              {/* Global CSS Style Card */}
              <Card
                title={
                  <Flex align="center" gap={8}>
                    <CodeOutlined style={{ color: token.colorPrimary }} />
                    <span style={{ fontWeight: 600 }}>全局附加样式</span>
                  </Flex>
                }
                bordered
                style={{ 
                  borderRadius: token.borderRadiusLG,
                  borderColor: token.colorBorderSecondary,
                  boxShadow: "0 2px 8px rgba(0,0,0,0.02)"
                }}
              >
                <Form.Item
                  name="styleGlobal"
                  tooltip="这里配置的 CSS 会注入到前台聚合页，可按需要覆盖 .arranger-widget 等样式。"
                  style={{ marginBottom: 0 }}
                >
                  <Input.TextArea
                    rows={12}
                    placeholder="/* 输入要注入前台聚合页的 CSS */\n\n.arranger-widget {\n  border-radius: 8px;\n}"
                    style={{ 
                      fontFamily: "Fira Code, Menlo, Monaco, Consolas, Courier New, monospace",
                      fontSize: 13,
                      borderRadius: 6,
                      backgroundColor: token.colorBgLayout
                    }}
                  />
                </Form.Item>
              </Card>
            </Space>
          </Col>

          {/* Right Column: Article Groups Checklist */}
          <Col xs={24} lg={14}>
            <Card
              title={
                <Flex align="center" gap={8}>
                  <SettingOutlined style={{ color: token.colorPrimary }} />
                  <span style={{ fontWeight: 600 }}>聚合文章</span>
                </Flex>
              }
              extra={
                <Text style={{ fontSize: 13 }} type="secondary">
                  已选择 <Text strong type="warning">{selectedIds.length}</Text> 篇文章
                </Text>
              }
              bordered
              style={{ 
                borderRadius: token.borderRadiusLG,
                borderColor: token.colorBorderSecondary,
                boxShadow: "0 2px 8px rgba(0,0,0,0.02)",
                minHeight: isCompact ? undefined : 520
              }}
            >
              {data.config.groups.length === 0 ? (
                <Flex vertical justify="center" align="center" style={{ minHeight: 300 }}>
                  <Text type="secondary" style={{ fontSize: 14 }}>
                    当前没有分类或文章，请先在主站创建文章。
                  </Text>
                </Flex>
              ) : (
                <Space direction="vertical" size={24} style={{ width: "100%" }}>
                  {data.config.groups.map((group, index) => {
                    const groupItemIds = group.items.map(item => item.id);
                    const allSelected = groupItemIds.length > 0 && groupItemIds.every(id => selectedIds.includes(id));

                    return (
                      <Card
                        key={group.name}
                        type="inner"
                        bordered
                        title={
                          <Flex justify="space-between" align="center" style={{ width: "100%" }}>
                            <span style={{ fontSize: 14, fontWeight: 650 }}>
                              分类：{group.name}
                            </span>
                            {group.items.length > 0 && (
                              <Button 
                                type="text"
                                size="small"
                                icon={allSelected ? <BorderOutlined /> : <CheckSquareOutlined />}
                                onClick={() => toggleCategory(group)}
                                style={{ fontSize: 12, padding: "0 8px" }}
                              >
                                {allSelected ? "取消全选" : "分类全选"}
                              </Button>
                            )}
                          </Flex>
                        }
                        style={{ 
                          borderRadius: 8,
                          boxShadow: "0 1px 4px rgba(0,0,0,0.01)",
                          borderColor: token.colorBorder
                        }}
                      >
                        {group.items.length === 0 ? (
                          <Text type="secondary" style={{ fontStyle: "italic", fontSize: 13 }}>
                            该分类下当前无任何文章。
                          </Text>
                        ) : (
                          <Row gutter={[12, 12]}>
                            {group.items.map((item) => {
                              const checked = selectedIds.includes(item.id);
                              return (
                                <Col xs={24} sm={12} key={item.id}>
                                  <div
                                    onClick={() => toggleArticle(item.id)}
                                    style={{
                                      display: "flex",
                                      alignItems: "center",
                                      padding: "10px 14px",
                                      border: `1px solid ${checked ? token.colorPrimary : token.colorBorderSecondary}`,
                                      borderRadius: 6,
                                      backgroundColor: checked ? `${token.colorPrimary}06` : token.colorBgContainer,
                                      cursor: "pointer",
                                      transition: "all 0.2s",
                                      boxSizing: "border-box"
                                    }}
                                    className="article-check-item"
                                  >
                                    <Checkbox 
                                      checked={checked}
                                      style={{ marginRight: 10 }}
                                      onClick={(e) => e.stopPropagation()}
                                      onChange={() => toggleArticle(item.id)}
                                    />
                                    <div style={{ flex: 1, minWidth: 0 }}>
                                      <Text 
                                        ellipsis={{ tooltip: item.title }}
                                        style={{ 
                                          fontSize: 13, 
                                          fontWeight: checked ? 600 : 400,
                                          color: checked ? token.colorPrimary : token.colorText
                                        }}
                                      >
                                        {item.title}
                                      </Text>
                                    </div>
                                  </div>
                                </Col>
                              );
                            })}
                          </Row>
                        )}
                      </Card>
                    );
                  })}
                </Space>
              )}
            </Card>
          </Col>
        </Row>
      </Form>
    </div>
  );
};

export default AppBase;
