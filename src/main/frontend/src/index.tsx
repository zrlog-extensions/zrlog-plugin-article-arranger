import axios from "axios";
import {useEffect, useState} from "react";
import {createRoot} from "react-dom/client";
import AppBase from "./AppBase";
import {PluginThemeProvider} from "./PluginThemeProvider";
import type {PluginAppearance} from "./PluginThemeProvider";

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
    selected?: boolean;
}

export interface CategoryGroup {
    id?: number;
    alias?: string;
    name: string;
    typeName?: string;
    typeUrl?: string;
    arrange_plugin?: string;
    selected?: boolean;
    items: ArticleItem[];
}

export interface ConfigData {
    styleGlobal: string;
    mainColor: string;
    groups: CategoryGroup[];
}

export interface ArrangerInfoResponse extends PluginAppearance {
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
        <PluginThemeProvider page={response.data}>
            <AppBase data={response.data} setResponse={setResponse}/>
        </PluginThemeProvider>
    );
};

const container = document.getElementById("app");
const root = createRoot(container!);
root.render(<Index/>);
