<!DOCTYPE html>
<html>
<head>
    <title>${_plugin.name}设置</title>
    <meta charset="utf-8"/>
    <link rel="stylesheet" href="assets/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="assets/css/jquery.gritter.css"/>

    <script src="assets/js/jquery-2.0.3.min.js"></script>
    <script src="assets/js/jquery.gritter.min.js"></script>
    <script src="js/setting.js"></script>
    <style>
        body {
            margin: 0;
            font-size: 16px;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
            font-variant: tabular-nums;
            line-height: 1.5;
            font-feature-settings: 'tnum';
            background: transparent;
        }

        .dark {
            background-color: #000;
            color: #fff
        }

        .mb-3 {
            display: flex;
            align-items: center;
        }

        .form-horizontal {
            text-align: end;
        }

        @media (max-width: 600px) {
            .form-horizontal {
                text-align: start;
            }
        }
    </style>
</head>
<body style="margin: 0" class="${theme}">
<div id="vue-div" style="width: 100%;max-width: 960px">
    <div class="page-header" style="padding-top: 20px">
        <h3>
            ${_plugin.name}设置
        </h3>
    </div>
    <hr style="color: #a4a1a1"/>
    <form role="form" id="ajaxarticleArranger" class="form-horizontal">
        <div class="form-group mb-3 row">
            <label class="col-md-3 control-label no-padding-right" for="styleGlobal"> 全局样式 </label>
            <div class="col-md-9">
                <textarea  id="styleGlobal" type="text" placeholder="" rows="8" class="form-control"
                           name="styleGlobal">${styleGlobal}</textarea>
            </div>
        </div>
        <div class="form-group mb-3 row">
            <label class="col-md-3 control-label no-padding-right" for="mainColor"> 主颜色 </label>
            <div class="col-md-4">
                <input  id="mainColor" type="text" placeholder="" class="form-control"
                           name="mainColor">${mainColor!'#007BFF'}</input>
            </div>
        </div>
        <#list groups as group>
        <div class="form-group mb-3 row">
            <label class="col-md-3 control-label no-padding-right" > 分组（${group.name}） </label>
            <div class="col-md-4">
                <#list group.items as item>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="item" value="" id="flexCheckDefault-item-${item.id}">
                        <label class="form-check-label" for="flexCheckDefault-item-${item.id}">
                            ${item.title}
                        </label>
                    </div>
                </#list>
            </div>
        </div>
        </#list>
        <hr style="color: #a4a1a1"/>
        <div class="form-group row mb-3">
            <div class="col-sm-3"></div>
            <button class="btn btn-primary" type="button" id="articleArranger" style="max-width: 80px;margin-left: 12px">
                提交
            </button>
        </div>
    </form>
</div>
<input id="gritter-light" checked="" type="checkbox" style="display:none"/>
</body>
</html>