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

    }
    /* Dark Mode Styles */
    @media (prefers-color-scheme: dark) {
        .arranger-widget > .arranger-article {
            box-shadow: 0 0 2px rgba(198, 198, 198, 0.5);
        }
    }

    /* Styles for smaller screens */
    @media (max-width: 600px) {
        .toggle-button {
            display: block;
            position: absolute;
            right: 0;
            top: 0;
            padding: 10px;
            background-color: var(--main-color);
            color: white;
            border: none;
            cursor: pointer;
            border-radius: 4px;
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

        .arranger-outline {
            display: block;
            position: absolute;
            right: 0;
            top: 44px;
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
                <li class="<#if item.active>active</#if>">
                    <a href="${item.url}">${item.title}</a>
                </li>
            </#list>
        </ul>
    </div>
    <div class="arranger-article">
        <div class="arranger-title">
            <h2>${title!''}</h2>
            <button class="toggle-button"><img width="20" src="data:image/svg+xml,%3Csvg%20xmlns%3D'http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg'%20viewBox%3D'0%200%2030%2030'%3E%3Cpath%20stroke%3D'rgba(255%2C%20255%2C%20255%2C%200.55)'%20stroke-linecap%3D'round'%20stroke-miterlimit%3D'10'%20stroke-width%3D'2'%20d%3D'M4%207h22M4%2015h22M4%2023h22'%2F%3E%3C%2Fsvg%3E"></button>
        </div>
        <hr/>
        <div class="arranger-content markdown-body">${content!''}</div>
    </div>
</div>