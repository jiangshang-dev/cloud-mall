# chatgpt-views

Vue3 + Vite + JavaScript 用户端（AI 套餐自助充值 / 我的订单）。

## 依赖安装

```bash
cd chatgpt-views
pnpm install   # 或 npm install
```

若本机 registry 不便，可临时借用旁路工程依赖：

```bash
ln -sfn ../../cloud-mall-views/node_modules node_modules
```

## 启动

```bash
pnpm dev
# 或 ./node_modules/.bin/vite --host 127.0.0.1 --port 5173
```

浏览器打开 http://localhost:5173  

接口代理到 `http://localhost:8080/jeecg-boot`。

## 演示卡密

| 卡密 | 商品 |
|------|------|
| `PLUS-DEMO-0001-AAAA` | 1个月 Plus |
| `GO-DEMO-0001-CCCC` | 1个月 Go |
| `5X-DEMO-0001-DDDD` | 5x 额度包 |

后端需先启动 `com.mall.MallApplication`（`mall-web`）。首次需确保库表已建（脚本：`V3.9.5_2__mall_cdk_order.sql`）。

交付为**占位自动完成**，不接入任何 ChatGPT session/token 代操作。
