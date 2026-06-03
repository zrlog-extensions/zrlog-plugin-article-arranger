import {legacyLogicalPropertiesTransformer, StyleProvider} from "@ant-design/cssinjs";
import {App, ConfigProvider, Layout, theme} from "antd";
import zhCN from "antd/es/locale/zh_CN";
import axios from "axios";
import {useEffect, useState} from "react";
import {createRoot} from "react-dom/client";
import AppBase from "./AppBase";

const {darkAlgorithm, defaultAlgorithm} = theme;
const {Content} = Layout;

export interface Plugin {
    id: string;
    version: string;
    name: string;
    desc: string;
    author: string;
    shortName: string;
}

export interface ArticleItem {
    id: number;
    title: string;
    arrange_plugin?: string;
}

export interface CategoryGroup {
    name: string;
    items: ArticleItem[];
}

export interface ConfigData {
    styleGlobal: string;
    mainColor: string;
    groups: CategoryGroup[];
}

export interface ArrangerInfoResponse {
    dark: boolean;
    colorPrimary: string;
    plugin: Plugin;
    config: ConfigData;
}

export interface StandardResponse<T> {
    success: boolean;
    message?: string;
    data: T;
}

const loadFromDocument = () => {
    try {
        const node = document.getElementById("pluginInfo");
        if (node === null || node.innerText.length === 0) {
            return null;
        }
        const text = node.innerText.trim();
        if (text.startsWith("${") && text.endsWith("}")) {
            return null;
        }
        return JSON.parse(text) as StandardResponse<ArrangerInfoResponse>;
    } catch (e) {
        return null;
    }
};

const Index = () => {
    const [response, setResponse] = useState<StandardResponse<ArrangerInfoResponse> | null>(loadFromDocument);

    useEffect(() => {
        if (response === null) {
            axios.get<StandardResponse<ArrangerInfoResponse>>("json").then(({data}) => {
                setResponse(data);
            });
        }
    }, [response]);

    if (response === null || !response.success) {
        return <></>;
    }

    return (
        <ConfigProvider
            locale={zhCN}
            theme={{
                algorithm: response.data.dark ? darkAlgorithm : defaultAlgorithm,
                token: {
                    colorPrimary: response.data.colorPrimary || "#007BFF",
                },
            }}
        >
            <StyleProvider transformers={[legacyLogicalPropertiesTransformer]}>
                <Content style={{minHeight: "100vh", backgroundColor: response.data.dark ? "#141414" : undefined, color: response.data.dark ? "#dfdfdf" : undefined}}>
                    <App>
                        <AppBase data={response.data} setResponse={setResponse}/>
                    </App>
                </Content>
            </StyleProvider>
        </ConfigProvider>
    );
};

const container = document.getElementById("app");
const root = createRoot(container!);
root.render(<Index/>);
