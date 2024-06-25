<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>

    :root {
        --main-color: ${mainColor!'#007BFF'};
    }

    .arranger-widget {
        display: flex;
        justify-content: flex-start;
        gap: 16px;
        position: relative;
    }

    .arranger-widget .arranger-article .arranger-title {
        display: flex;
        align-items: center;
        justify-content: space-between;
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
        display: flex;
        flex-flow: column;
        gap: 4px;
    }

    .arranger-outline ul li {
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
        padding: 12px;
        border: 1px solid #f5f5f5;
        border-radius: 2px;
        transition: background-color 0.3s, transform 0.3s;
    }

    .arranger-outline ul li a {
        text-decoration: none;
        color: var(--main-color);
        display: block;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .arranger-outline ul li:hover {
        transform: translateY(-1px);
    }

    .arranger-outline ul li.active {
        border-left: 4px var(--main-color) solid;
        padding-left: 8px;
    }

    .arranger-widget > .arranger-article {
        width: 100%;
        max-width: 884px;
        padding: 8px;
        box-shadow: 0 0 2px rgb(0 0 0 / 20%);
    }

    .arranger-widget > .arranger-article > .arranger-title h2 {
        margin-top: 0;
        margin-bottom: 0;
    }

    .arranger-widget > .arranger-article > .arranger-content {
        padding-bottom: 32px;
    }

    /* Dark Mode Styles */
    @media (prefers-color-scheme: dark) {
        .arranger-widget > .arranger-article {
            box-shadow: 0 0 2px rgba(198, 198, 198, 0.5);
        }

        .arranger-outline ul li {
            border: 1px solid rgb(55 58 62);
        }
    }

    /* Styles for smaller screens */
    @media (max-width: 600px) {
        .toggle-button {
            display: flex;
            padding: 10px;
            background-color: var(--main-color);
            color: white;
            border: none;
            align-items: center;
            cursor: pointer;
            border-radius: 4px;
        }

        .arranger-outline ul li {
            border: none;
        }

        .arranger-outline ul {
            /*default hide*/
            display: none;
        }

        .menu-list {
            display: none;
        }

        .menu-list.show {
            display: block;
            background: white;
            border-radius: 4px;
        }

        .arranger-outline ul li.active {
            background: #dadada;
        }

        .arranger-outline ul li {
            margin-bottom: 0;
        }

        .arranger-outline {
            display: block;
            position: absolute;
            right: 8px;
            top: 48px;
            z-index: 1;
        }
    }

    /* Styles for larger screens */
    @media (min-width: 601px) {
        .toggle-button {
            display: none;
        }

        .menu-list {
            display: block;
        }
    }

</style>
<meta charset="UTF-8">
${styleGlobal}
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const toggleButton = document.querySelector('.toggle-button');
        const menuList = document.querySelector('.menu-list');

        toggleButton.addEventListener('click', function () {
            menuList.classList.toggle('show');
        });
    });
</script>
<div class="arranger-widget">
    <div class="arranger-outline">
        <ul class="menu-list">
            <#list items as item>
                <li class="<#if item.active??>active</#if>">
                    <a href="${item.url}">${item.title}</a>
                </li>
            </#list>
        </ul>
    </div>
    <div class="arranger-article">
        <div class="arranger-title">
            <h2>${title!''}</h2>
            <button class="toggle-button">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 30 30" style="width: 20px;display: flex;">
                    <path stroke="rgba(255, 255, 255, 0.55)" stroke-linecap="round" stroke-miterlimit="10"
                          stroke-width="2" d="M4 7h22M4 15h22M4 23h22"></path>
                </svg>
            </button>
        </div>
        <hr/>
        <div class="arranger-content markdown-body">${content!''}</div>
    </div>
</div>