package com.cloud.zlist.autofire;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author lingrui
 * @version 1.0
 * @Title: ListAutoFire
 * @Description: 分析网页列表结构
 * @Company: 北京华宇元典信息服务有限公司
 * @date 2018年8月15日 下午13:44:32
 */
public class ListAutoFire {

    /**
     * 获取叶子节点选择器
     *
     * @param node
     */
    public static List<String> getYeziNodeSel(Element node) {
        List<String> list = new ArrayList<String>();
        Elements all = node.getAllElements();
        for (Element item : all) {
            if (item.children().isEmpty()) list.add(item.cssSelector());
        }
        return list;
    }

    /**
     * 统计列表下各个一级节点类型及个数
     *
     * @param node
     * @return
     */
    private Map<String, Integer> getGroupNode(Element node) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        Elements children = node.children();
        for (Element item : children) {
            if (KeysEnum.input.equalsIgnoreCase(item.tagName()) || KeysEnum.br.equalsIgnoreCase(item.tagName())
                    || KeysEnum.script.equalsIgnoreCase(item.tagName()) || KeysEnum.link.equalsIgnoreCase(item.tagName())
                    || KeysEnum.style.equalsIgnoreCase(item.tagName()) || KeysEnum.meta.equalsIgnoreCase(item.tagName())
                    || KeysEnum.select.equalsIgnoreCase(item.tagName()) || KeysEnum.option.equalsIgnoreCase(item.tagName())
                    || KeysEnum.video.equals(item.tagName()) || KeysEnum.audio.equals(item.tagName())
                    || KeysEnum.textarea.equals(item.tagName())) continue;
            String key = filterHtml(item);
            if (map.containsKey(key)) {
                map.put(key, (Integer) map.get(key) + 1);
            } else {
                boolean is_like = false;
                for (String map_key : map.keySet()) {
                    int dis = SimilarDegree.EditDistance(key, (String) map_key);
                    float v = (float) (key.length() - dis) / key.length();
                    if (v > SimilarDegree.degree) {
                        map.put(map_key, (Integer) map.get(map_key) + 1);
                        is_like = true;
                        break;
                    }
                }
                if (!is_like) map.put(key, 1);
            }
        }
        return map;
    }

    /**
     * 获取节点下相同或者相似的dom结构的key和出现数量
     *
     * @param node
     * @return
     */
    private Map<String, Object> getMaxKeyDom(Element node) {
        Map<String, Integer> map = getGroupNode(node);
        String key = StringUtils.EMPTY;
        int max = 0;
        for (Object item : map.keySet()) {
            Integer o = (Integer) map.get(item);
            String tk = (String) item;
            if (o >= max || (o == max && key.length() <= tk.length())) {
                max = o;
                key = tk;
            }
        }
        Map<String, Object> res = new HashMap<String, Object>();
        res.put(KeysEnum.max_key, key);
        res.put(KeysEnum.max_num, max);
        return res;
    }

    /**
     * 分析元素dom结构框架
     *
     * @param node
     * @return
     */
    public String filterHtml(Element node) {
        //去除节点的属性值
        Document new_node = Jsoup.parse(node.outerHtml());
        Elements elements = new_node.getAllElements();
        for (Element item : elements) {
            Attributes attributes = item.attributes();
            for (Attribute a : attributes) {
                if (a.getKey().equals(KeysEnum.attr_scroce)) {
                    item.removeAttr(a.getKey());
                    continue;
                }
                a.setValue(StringUtils.EMPTY);
            }
        }
        //去除注释节点,节点文本内容
        String str_new = new_node.outerHtml().replaceAll("<!--?(.*?)-->", "");
        str_new = str_new.replaceAll("\\s*", "");
        str_new = str_new.replaceAll(">(.*?)<", "><");
        return str_new;
    }

    /**
     * 排序子节点
     * 1.最大相同dom结构长度
     * 2.最大相同dom结构元素数量
     *
     * @param nodes
     * @return
     */
    private Elements sortBy(Elements nodes, String base_url) {
//        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        nodes.sort(new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                double o1_rate = reckonRate(o1);
                double o2_rate = reckonRate(o2);
                return (o2_rate > o1_rate) ? 1 : ((o2_rate == o1_rate) ? 0 : -1);
            }

            private double reckonRate(Element o) {
                if (StringUtils.isNotBlank(base_url) && KeysEnum.a.equalsIgnoreCase(o.tagName()) && base_url.equalsIgnoreCase(o.attr(KeysEnum.attr_href)))
                    o.attr(KeysEnum.attr_list_tag_name, o.text());
                if (null == o || o.children().size() < 2
                        || KeysEnum.html.equalsIgnoreCase(o.tagName()) || KeysEnum.body.equalsIgnoreCase(o.tagName()) || KeysEnum.link.equalsIgnoreCase(o.tagName())
                        || KeysEnum.head.equalsIgnoreCase(o.tagName()) || KeysEnum.title.equalsIgnoreCase(o.tagName()) || KeysEnum.meta.equalsIgnoreCase(o.tagName())
                        || KeysEnum.script.equalsIgnoreCase(o.tagName()) || KeysEnum.style.equalsIgnoreCase(o.tagName())) {
                    o.attr(KeysEnum.attr_scroce, "0");
                    return 0;
                }
                String style = o.attr(KeysEnum.style);
                if (StringUtils.isNotBlank(style) && style.contains(KeysEnum.display_none)) {
                    o.attr(KeysEnum.attr_scroce, "0");
                    return 0;
                }
                Map<String, Object> maxKeyDom = getMaxKeyDom(o);
                String key = (String) maxKeyDom.get(KeysEnum.max_key);
                int num = (int) maxKeyDom.get(KeysEnum.max_num);
                if (num < 2) {
                    o.attr(KeysEnum.attr_scroce, "0");
                    return 0;
                }
                int scroce = num * key.length();
                Elements tags = o.children();
                for (Element a : tags) {
                    if (KeysEnum.div.equalsIgnoreCase(a.tagName())) scroce += 5;
                    if (KeysEnum.ul.equalsIgnoreCase(a.tagName())) scroce += 10;
                    if (KeysEnum.li.equalsIgnoreCase(a.tagName())) scroce += 10;
                    if (KeysEnum.tbody.equalsIgnoreCase(a.tagName())) scroce += 5;
                    if (KeysEnum.table.equalsIgnoreCase(a.tagName())) scroce += 5;
                    if (KeysEnum.tr.equalsIgnoreCase(a.tagName())) scroce += 10;
                    if (KeysEnum.td.equalsIgnoreCase(a.tagName())) scroce += 1;
                    if (KeysEnum.a.equalsIgnoreCase(a.tagName())) scroce += 1;
                    if (KeysEnum.p.equalsIgnoreCase(a.tagName())) scroce += 1;
                    try {
                        Date time = DateParser.parse(a.text());
                        if (null != time) scroce += 20;
                    } catch (Exception e) {
                    }
                }
                if (o.text().contains(KeysEnum.next_page)) scroce += 100;
                if (o.text().contains(KeysEnum.start_page) || o.text().contains(KeysEnum.fisrt_page)) scroce += 100;
                if (o.text().contains(KeysEnum.end_page) || o.text().contains(KeysEnum.last_page) || o.text().contains(KeysEnum.final_page))
                    scroce += 100;
                o.attr(KeysEnum.attr_scroce, String.valueOf(scroce));
                return scroce;
            }
        });
        return nodes;
    }

    /**
     * 自动发现网页中列表节点
     *
     * @param document
     * @return
     */
    public Element autoFireListNodeFirst(Document document) {
        return autoFireListNodes(document).get(0);
    }

    /**
     * 自动发现网页中列表节点
     *
     * @param document
     * @return
     */
    public Elements autoFireListNodes(Document document) {
        if (null == document) return null;
        Elements nodes = document.getAllElements();
        nodes = sortBy(nodes, document.baseUri());
        return nodes;
    }

    /**
     * 提取页面列表元素的选择器以及页面分类标签
     *
     * @param document
     * @param is_subitem
     * @return
     */
    public static Map<String, Object> dealListNode(Document document, boolean is_subitem) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            ListAutoFire listAutoFire = new ListAutoFire();
            Elements list_node = listAutoFire.autoFireListNodes(document);
            List<Map<String, Object>> lists = new ArrayList();
            if (null != list_node && list_node.size() > 0) {
                for (Element list_sel_item : list_node) {
                    if (list_sel_item.hasAttr(KeysEnum.attr_list_tag_name) && StringUtils.isNotBlank(list_sel_item.attr(KeysEnum.attr_list_tag_name))) {
                        result.put(KeysEnum.tag_name, list_sel_item.attr(KeysEnum.attr_list_tag_name));
                        continue;
                    }
                    Map<String, Object> list_dom_frame = new HashMap<>();
                    list_dom_frame.put(KeysEnum.list_sel, list_sel_item.cssSelector());
                    if (is_subitem) {
                        Map<String, List<String>> listItem = new HashMap<String, List<String>>();
                        for (Element item : list_sel_item.children())
                            listItem.put(item.cssSelector(), getYeziNodeSel(item));
                        list_dom_frame.put(KeysEnum.list_dom, listItem);
                    }
                    list_dom_frame.put(KeysEnum.attr_scroce, list_sel_item.attr(KeysEnum.attr_scroce));
                    lists.add(list_dom_frame);
                }
            }
            result.put(KeysEnum.list, lists);
        } catch (Exception e) {
            throw new Exception(KeysEnum.error_info, e.getCause());
        }
        return result;
    }

    /**
     * 处理网页结构
     *
     * @param home_url   入口地址
     * @param list_index 列表元素获取数量
     * @param is_subitem 是否处理列表元素子项抽取 true/false
     * @param is_ifr     是否处理iframe true/false
     * @return
     */
    public static Map<String, Object> getWebSiteFrame(String home_url, int list_index, boolean is_subitem, boolean is_ifr) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (StringUtils.isBlank(home_url)) return result;
        try {
            Document html = Jsoup.connect(home_url).ignoreContentType(true).validateTLSCertificates(false).timeout(5000).get();
            if (null == html) throw new Exception(KeysEnum.open_fail);
            Map<String, Object> mapNode = dealListNode(html, is_subitem);
            List listNode = (List) mapNode.get(KeysEnum.list);
            result.put(KeysEnum.home_url, home_url);
            result.put(KeysEnum.tag_name, mapNode.get(KeysEnum.tag_name));
            result.put(KeysEnum.list, listNode.subList(0, listNode.size() > list_index ? list_index : listNode.size()));
            result.put(KeysEnum.ifrs, new ArrayList());
            if (is_ifr) {
                List<Map<String, Object>> ifrs = (List<Map<String, Object>>) result.get(KeysEnum.ifrs);
                Elements iframe_nodes = html.getElementsByTag(KeysEnum.iframe);
                if (null != iframe_nodes) {
                    for (Element iframe : iframe_nodes) {
                        String iframe_url = iframe.attr(KeysEnum.attr_src);
                        if (StringUtils.isBlank(iframe_url)) continue;
                        try {
                            Document iframe_html = Jsoup.connect(iframe_url).ignoreContentType(true).validateTLSCertificates(false).timeout(5000).get();
                            if (null == iframe_html) continue;
                            Map<String, Object> ifrMapNode = dealListNode(iframe_html, is_subitem);
                            List ifrListNode = (List) ifrMapNode.get(KeysEnum.list);
                            Map<String, Object> ifr_map = new HashMap();
                            ifr_map.put(KeysEnum.home_url, iframe_url);
                            ifr_map.put(KeysEnum.tag_name, ifrMapNode.get(KeysEnum.tag_name));
                            ifr_map.put(KeysEnum.list, ifrListNode.subList(0, ifrListNode.size() > list_index ? list_index : ifrListNode.size()));
                            ifrs.add(ifr_map);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.put(KeysEnum.home_url, home_url);
            result.put(KeysEnum.error, KeysEnum.error_info);
            result.put(KeysEnum.message, e.toString());
        }
        return result;
    }

    /**
     * 处理网页结构
     *
     * @param home_url   入口地址
     * @param list_index 列表元素获取数量
     * @param is_subitem 是否处理列表元素子项抽取 true/false
     * @return
     */
    public static Map<String, Object> getWebSiteFrame(String home_url, int list_index, boolean is_subitem) {
        return getWebSiteFrame(home_url, list_index, is_subitem, false);
    }

    /**
     * 处理网页结构
     *
     * @param home_url   入口地址
     * @param list_index 列表元素获取数量
     * @return
     */
    public static Map<String, Object> getWebSiteFrame(String home_url, int list_index) {
        return getWebSiteFrame(home_url, list_index, false);
    }

    /**
     * 处理网页结构
     *
     * @param home_url 入口地址
     * @return
     */
    public static Map<String, Object> getWebSiteFrame(String home_url) {
        return getWebSiteFrame(home_url, 10);
    }

    interface KeysEnum {
        // 标签字段常量
        String html = "html", body = "body", meta = "meta", script = "script", link = "link", style = "style", head = "head", title = "title";
        String input = "input", br = "br", select = "select", option = "option", video = "video", audio = "audio", textarea = "textarea", div = "div";
        String ul = "ul", li = "li", table = "table", tbody = "tbody", tr = "tr", td = "td", a = "a", p = "p", iframe = "iframe";
        // 标签属性字段常量
        String attr_href = "abs:href", attr_list_tag_name = "list_tag_name", attr_scroce = "scroce", attr_src = "abs:src";
        // key字段常量
        String max_key = "max_key", max_num = "max_num", ifrs = "ifrs", tag_name = "tag_name";
        String list_sel = "list_sel", list_dom = "list_dom", home_url = "home_url", list = "list";
        // 字符串常量
        String start_page = "首页", fisrt_page = "第一页", end_page = "末页", last_page = "尾页";
        String final_page = "最后一页", next_page = "下一页", display_none = "display:none";
        String error = "error", message = "message", error_info = "ListAutoFire Fail", open_fail = "home_url open fail";
    }

    public static void createMarkFile(Map siteFrame, String home_url, String path) {
        try {
            Document doc = Jsoup.connect(home_url).ignoreContentType(true).validateTLSCertificates(false).timeout(5000).get();
            if (null == doc) return;
            String style = ".mark_color {" +
                    "position:relative;" +
                    "pointer-events:none;" +
                    "left:0px;top:0px;" +
                    "display:inline-block;" +
                    "margin:-2px;width:100%;" +
                    "height:100%;" +
                    "border:dashed 2px #FF69B4;" +
                    "background-color: #43CD80;" +
                    "opacity:0.75;" +
                    "} ";
            List list = (List) siteFrame.get("list");
            for (Object item : list) {
                Map item_map = (Map) item;
                String sel = (String) item_map.get("list_sel");
                doc.select(sel).addClass("mark_color");
            }
            String content = doc.html();
            content = content.contains("<base") ? content : content.replaceFirst("<head", "<base href='" + home_url + "'/><style>" + style + "</style><head");
            FileUtils.writeStringToFile(new File(path), content, "UTF-8", false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        long start_time = new Date().getTime();
        System.out.println("-----------执行开始:" + start_time + "------------------------");
        String home_url = "https://www.cnblogs.com/";
        Map siteFrame = getWebSiteFrame(home_url, 10, true, true);
        System.out.println("website frame : \n\n" + JSONObject.valueToString(siteFrame).toString() + "\n");
        long end_time = new Date().getTime();
        System.out.println("-----------执行结束:" + end_time + ",耗时:" + (end_time - start_time) + "ms -----------");
        createMarkFile(siteFrame, home_url, "F:\\data\\autofire\\" + Md5Utils.GetMD5Code(home_url) + ".html");
    }

}

