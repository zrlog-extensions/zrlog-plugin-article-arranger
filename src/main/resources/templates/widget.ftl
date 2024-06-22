<style>
    .arranger-widget {
        display: flex;
        justify-content: flex-start;
        gap: 16px;
    }
    .arranger-widget > .arranger-outline {
        width: 240px;
    }
    .arranger-outline {
        width: 240px;
    }
    .arranger-outline ul {
        list-style: none;
        overflow: hidden;
        padding-left: 0;
        margin: 0;
    }
    .arranger-outline ul li {
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
        padding: 8px 12px;
        margin-bottom: 4px;
        border: 1px solid #ddd;
        border-radius: 2px;
        transition: background-color 0.3s, transform 0.3s;
    }
    .arranger-outline ul li a {
        text-decoration: none;
        color: #007BFF;
        display: block;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .arranger-outline ul li:hover {
        transform: translateY(-1px);
    }

    .arranger-outline ul li.active {
        border-left: 4px #007BFF solid;
    }

    .arranger-widget > .arranger-article {
        width: 884px;
        padding: 8px;
        box-shadow: 0 0 2px rgb(0 0 0 / 20%);
    }
    .arranger-widget > .arranger-article > .arranger-title {
        margin-top: 0;
    }
    .arranger-widget > .arranger-article > .arranger-content {

    }
    /* Dark Mode Styles */
    @media (prefers-color-scheme: dark) {
        .arranger-widget > .arranger-article {
            box-shadow: 0 0 2px rgba(198, 198, 198, 0.5);
        }
    }
</style>
<meta charset="UTF-8">
${styleGlobal}
<div class="arranger-widget">
    <div class="arranger-outline">
        <ul>
            <#list items as item>
                <li class="<#if item.active>active</#if>">
                    <a href="${item.url}">${item.title}</a>
                </li>
            </#list>
        </ul>
    </div>
    <div class="arranger-article">
        <h2 class="arranger-title">${title!''}</h2>
        <hr/>
        <div class="arranger-content markdown-body">${content!''}</div>
    </div>
</div>