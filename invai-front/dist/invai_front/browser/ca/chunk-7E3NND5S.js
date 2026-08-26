import{a as me,s as _e}from"./chunk-G7S5H2NB.js";import{c as fe}from"./chunk-IUOMPAFK.js";import{$ as ge,Aa as R,F as pe,I as he,K as H,L as O,R as d,S as F,ca as ve,n as y,o as z,p as M,ua as be,wa as Ae}from"./chunk-654V6O7A.js";import{C as G,G as ue}from"./chunk-XH2CBF5U.js";import{c as se,e as le,h as S}from"./chunk-VOBIKNA5.js";import{$b as re,Ab as k,Ac as c,Bb as v,Fb as $,Fc as de,Gb as U,Gc as w,Ib as ne,Jc as j,Pb as I,Qa as l,R as b,Rb as h,S as W,Sb as N,Tb as x,Ub as oe,V as C,Wb as te,X as r,Xb as ie,aa as Y,ac as ae,ba as J,ca as K,cb as g,dc as u,gb as D,ha as V,hb as T,ib as _,lc as P,ma as X,nc as ce,pb as f,ra as A,sb as Z,tb as ee,yb as a,zb as B}from"./chunk-QNANVFCF.js";import{a as E}from"./chunk-WXQXMLLV.js";var ye=`
    .p-accordionpanel {
        display: flex;
        flex-direction: column;
        border-style: solid;
        border-width: dt('accordion.panel.border.width');
        border-color: dt('accordion.panel.border.color');
    }

    .p-accordionheader {
        all: unset;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: dt('accordion.header.padding');
        color: dt('accordion.header.color');
        background: dt('accordion.header.background');
        border-style: solid;
        border-width: dt('accordion.header.border.width');
        border-color: dt('accordion.header.border.color');
        font-weight: dt('accordion.header.font.weight');
        border-radius: dt('accordion.header.border.radius');
        transition:
            background dt('accordion.transition.duration'),
            color dt('accordion.transition.duration'),
            outline-color dt('accordion.transition.duration'),
            box-shadow dt('accordion.transition.duration');
        outline-color: transparent;
    }

    .p-accordionpanel:first-child > .p-accordionheader {
        border-width: dt('accordion.header.first.border.width');
        border-start-start-radius: dt('accordion.header.first.top.border.radius');
        border-start-end-radius: dt('accordion.header.first.top.border.radius');
    }

    .p-accordionpanel:last-child > .p-accordionheader {
        border-end-start-radius: dt('accordion.header.last.bottom.border.radius');
        border-end-end-radius: dt('accordion.header.last.bottom.border.radius');
    }

    .p-accordionpanel:last-child.p-accordionpanel-active > .p-accordionheader {
        border-end-start-radius: dt('accordion.header.last.active.bottom.border.radius');
        border-end-end-radius: dt('accordion.header.last.active.bottom.border.radius');
    }

    .p-accordionheader-toggle-icon {
        color: dt('accordion.header.toggle.icon.color');
    }

    .p-accordionpanel:not(.p-disabled) .p-accordionheader:focus-visible {
        box-shadow: dt('accordion.header.focus.ring.shadow');
        outline: dt('accordion.header.focus.ring.width') dt('accordion.header.focus.ring.style') dt('accordion.header.focus.ring.color');
        outline-offset: dt('accordion.header.focus.ring.offset');
    }

    .p-accordionpanel:not(.p-accordionpanel-active):not(.p-disabled) > .p-accordionheader:hover {
        background: dt('accordion.header.hover.background');
        color: dt('accordion.header.hover.color');
    }

    .p-accordionpanel:not(.p-accordionpanel-active):not(.p-disabled) .p-accordionheader:hover .p-accordionheader-toggle-icon {
        color: dt('accordion.header.toggle.icon.hover.color');
    }

    .p-accordionpanel:not(.p-disabled).p-accordionpanel-active > .p-accordionheader {
        background: dt('accordion.header.active.background');
        color: dt('accordion.header.active.color');
    }

    .p-accordionpanel:not(.p-disabled).p-accordionpanel-active > .p-accordionheader .p-accordionheader-toggle-icon {
        color: dt('accordion.header.toggle.icon.active.color');
    }

    .p-accordionpanel:not(.p-disabled).p-accordionpanel-active > .p-accordionheader:hover {
        background: dt('accordion.header.active.hover.background');
        color: dt('accordion.header.active.hover.color');
    }

    .p-accordionpanel:not(.p-disabled).p-accordionpanel-active > .p-accordionheader:hover .p-accordionheader-toggle-icon {
        color: dt('accordion.header.toggle.icon.active.hover.color');
    }

    .p-accordioncontent {
        display: grid;
        grid-template-rows: 1fr;
    }

    .p-accordioncontent-wrapper {
        min-height: 0;
    }

    .p-accordioncontent-content {
        border-style: solid;
        border-width: dt('accordion.content.border.width');
        border-color: dt('accordion.content.border.color');
        background-color: dt('accordion.content.background');
        color: dt('accordion.content.color');
        padding: dt('accordion.content.padding');
    }
`;var L=["*"],we=["toggleicon"],Se=n=>({active:n});function Me(n,s){}function He(n,s){n&1&&_(0,Me,0,0,"ng-template");}function Oe(n,s){if(n&1&&_(0,He,1,0,null,0),n&2){let e=h();a("ngTemplateOutlet",e.toggleicon)("ngTemplateOutletContext",ce(2,Se,e.active()));}}function Fe(n,s){if(n&1&&v(0,"span",4),n&2){let e=h(3);u(e.cn(e.cx("toggleicon"),e.pcAccordion.collapseIcon)),a("pBind",e.ptm("toggleicon")),f("aria-hidden",!0);}}function Be(n,s){if(n&1&&(K(),v(0,"svg",5)),n&2){let e=h(3);u(e.cx("toggleicon")),a("pBind",e.ptm("toggleicon")),f("aria-hidden",!0);}}function ke(n,s){if(n&1&&($(0),_(1,Fe,1,4,"span",2)(2,Be,1,4,"svg",3),U()),n&2){let e=h(2);l(),a("ngIf",e.pcAccordion.collapseIcon),l(),a("ngIf",!e.pcAccordion.collapseIcon);}}function Re(n,s){if(n&1&&v(0,"span",4),n&2){let e=h(3);u(e.cn(e.cx("toggleicon"),e.pcAccordion.expandIcon)),a("pBind",e.ptm("toggleicon")),f("aria-hidden",!0);}}function Le(n,s){if(n&1&&(K(),v(0,"svg",7)),n&2){let e=h(3);a("pBind",e.ptm("toggleicon")),f("aria-hidden",!0);}}function Ke(n,s){if(n&1&&($(0),_(1,Re,1,4,"span",2)(2,Le,1,2,"svg",6),U()),n&2){let e=h(2);l(),a("ngIf",e.pcAccordion.expandIcon),l(),a("ngIf",!e.pcAccordion.expandIcon);}}function Ve(n,s){if(n&1&&_(0,ke,3,2,"ng-container",1)(1,Ke,3,2,"ng-container",1),n&2){let e=h();a("ngIf",e.active()),l(),a("ngIf",!e.active());}}var $e=`
${ye}

/* For PrimeNG */
.p-accordionheader-toggle-icon.icon-start {
    order: -1;
}

.p-accordionheader:has(.p-accordionheader-toggle-icon.icon-start) {
    justify-content: flex-start;
    gap: dt('accordion.header.padding');
}

.p-accordionheader.p-ripple {
    overflow: hidden;
    position: relative;
}

.p-accordioncontent .p-motion {
    display: grid;
    grid-template-rows: 1fr;
}
`,Ue={root:"p-accordion p-component",panel:({instance:n})=>["p-accordionpanel",{"p-accordionpanel-active":n.active(),"p-disabled":n.disabled()}],header:"p-accordionheader",toggleicon:"p-accordionheader-toggle-icon",contentContainer:"p-accordioncontent",contentWrapper:"p-accordioncontent-wrapper",content:"p-accordioncontent-content"},m=(()=>{class n extends he{name="accordion";style=$e;classes=Ue;static ɵfac=(()=>{let e;return function(o){return(e||(e=A(n)))(o||n);};})();static ɵprov=W({token:n,factory:n.ɵfac});}return n;})();var Ee=new C("ACCORDION_PANEL_INSTANCE"),Ce=new C("ACCORDION_HEADER_INSTANCE"),De=new C("ACCORDION_CONTENT_INSTANCE"),Te=new C("ACCORDION_INSTANCE"),Ie=(()=>{class n extends O{$pcAccordionPanel=r(Ee,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(d,{self:!0});componentName="AccordionPanel";onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}pcAccordion=r(b(()=>Q));value=j(void 0);disabled=w(!1,{transform:e=>R(e)});active=c(()=>this.pcAccordion.multiple()?this.valueEquals(this.pcAccordion.value(),this.value()):this.pcAccordion.value()===this.value());valueEquals(e,t){return Array.isArray(e)?e.includes(t):e===t;}_componentStyle=r(m);static ɵfac=(()=>{let e;return function(o){return(e||(e=A(n)))(o||n);};})();static ɵcmp=g({type:n,selectors:[["p-accordion-panel"],["p-accordionpanel"]],hostVars:4,hostBindings:function(t,o){t&2&&(f("data-p-disabled",o.disabled())("data-p-active",o.active()),u(o.cx("panel")));},inputs:{value:[1,"value"],disabled:[1,"disabled"]},outputs:{value:"valueChange"},features:[P([m,{provide:Ee,useExisting:n},{provide:H,useExisting:n}]),D([d]),T],ngContentSelectors:L,decls:1,vars:0,template:function(t,o){t&1&&(N(),x(0));},dependencies:[S,F],encapsulation:2,changeDetection:0});}return n;})(),bn=(()=>{class n extends O{$pcAccordionHeader=r(Ce,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(d,{self:!0});componentName="AccordionHeader";onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}pcAccordion=r(b(()=>Q));pcAccordionPanel=r(b(()=>Ie));id=c(()=>`${this.pcAccordion.id()}_accordionheader_${this.pcAccordionPanel.value()}`);active=c(()=>this.pcAccordionPanel.active());disabled=c(()=>this.pcAccordionPanel.disabled());ariaControls=c(()=>`${this.pcAccordion.id()}_accordioncontent_${this.pcAccordionPanel.value()}`);toggleicon;onClick(e){if(this.disabled())return;let t=this.active();this.changeActiveValue();let o=this.active(),i=this.pcAccordionPanel.value();!t&&o?this.pcAccordion.onOpen.emit({originalEvent:e,index:i}):t&&!o&&this.pcAccordion.onClose.emit({originalEvent:e,index:i});}onFocus(){!this.disabled()&&this.pcAccordion.selectOnFocus()&&this.changeActiveValue();}onKeydown(e){switch(e.code){case"ArrowDown":this.arrowDownKey(e);break;case"ArrowUp":this.arrowUpKey(e);break;case"Home":this.onHomeKey(e);break;case"End":this.onEndKey(e);break;case"Enter":case"Space":case"NumpadEnter":this.onEnterKey(e);break;default:break;}}_componentStyle=r(m);changeActiveValue(){this.pcAccordion.updateValue(this.pcAccordionPanel.value());}findPanel(e){return e?.closest('[data-pc-name="accordionpanel"]');}findHeader(e){return y(e,'[data-pc-name="accordionheader"]');}findNextPanel(e,t=!1){let o=t?e:e.nextElementSibling;return o?M(o,"data-p-disabled")?this.findNextPanel(o):this.findHeader(o):null;}findPrevPanel(e,t=!1){let o=t?e:e.previousElementSibling;return o?M(o,"data-p-disabled")?this.findPrevPanel(o):this.findHeader(o):null;}findFirstPanel(){return this.findNextPanel(this.pcAccordion.el.nativeElement.firstElementChild,!0);}findLastPanel(){return this.findPrevPanel(this.pcAccordion.el.nativeElement.lastElementChild,!0);}changeFocusedPanel(e,t){z(t);}arrowDownKey(e){let t=this.findNextPanel(this.findPanel(e.currentTarget));t?this.changeFocusedPanel(e,t):this.onHomeKey(e),e.preventDefault();}arrowUpKey(e){let t=this.findPrevPanel(this.findPanel(e.currentTarget));t?this.changeFocusedPanel(e,t):this.onEndKey(e),e.preventDefault();}onHomeKey(e){let t=this.findFirstPanel();this.changeFocusedPanel(e,t),e.preventDefault();}onEndKey(e){let t=this.findLastPanel();this.changeFocusedPanel(e,t),e.preventDefault();}onEnterKey(e){this.disabled()||this.changeActiveValue(),e.preventDefault();}get dataP(){return this.cn({active:this.active()});}static ɵfac=(()=>{let e;return function(o){return(e||(e=A(n)))(o||n);};})();static ɵcmp=g({type:n,selectors:[["p-accordion-header"],["p-accordionheader"]],contentQueries:function(t,o,i){if(t&1&&oe(i,we,5),t&2){let p;te(p=ie())&&(o.toggleicon=p.first);}},hostVars:13,hostBindings:function(t,o){t&1&&I("click",function(p){return o.onClick(p);})("focus",function(){return o.onFocus();})("keydown",function(p){return o.onKeydown(p);}),t&2&&(f("id",o.id())("aria-expanded",o.active())("aria-controls",o.ariaControls())("aria-disabled",o.disabled())("role","button")("tabindex",o.disabled()?"-1":"0")("data-p-active",o.active())("data-p-disabled",o.disabled())("data-p",o.dataP),u(o.cx("header")),ae("user-select","none"));},features:[P([m,{provide:Ce,useExisting:n},{provide:H,useExisting:n}]),D([ge,d]),T],ngContentSelectors:L,decls:3,vars:1,consts:[[4,"ngTemplateOutlet","ngTemplateOutletContext"],[4,"ngIf"],[3,"class","pBind",4,"ngIf"],["data-p-icon","chevron-up",3,"class","pBind",4,"ngIf"],[3,"pBind"],["data-p-icon","chevron-up",3,"pBind"],["data-p-icon","chevron-down",3,"pBind",4,"ngIf"],["data-p-icon","chevron-down",3,"pBind"]],template:function(t,o){t&1&&(N(),x(0),Z(1,Oe,1,4)(2,Ve,2,2)),t&2&&(l(),ee(o.toggleicon?1:2));},dependencies:[S,se,le,fe,me,F,d],encapsulation:2,changeDetection:0});}return n;})(),An=(()=>{class n extends O{$pcAccordionContent=r(De,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(d,{self:!0});componentName="AccordionContent";onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}pcAccordion=r(b(()=>Q));pcAccordionPanel=r(b(()=>Ie));active=c(()=>this.pcAccordionPanel.active());ariaLabelledby=c(()=>`${this.pcAccordion.id()}_accordionheader_${this.pcAccordionPanel.value()}`);id=c(()=>`${this.pcAccordion.id()}_accordioncontent_${this.pcAccordionPanel.value()}`);_componentStyle=r(m);ptParams=c(()=>({context:this.active()}));computedMotionOptions=c(()=>E(E({},this.ptm("motion",this.ptParams())),this.pcAccordion.computedMotionOptions()));static ɵfac=(()=>{let e;return function(o){return(e||(e=A(n)))(o||n);};})();static ɵcmp=g({type:n,selectors:[["p-accordion-content"],["p-accordioncontent"]],hostVars:6,hostBindings:function(t,o){t&2&&(f("id",o.id())("role","region")("data-p-active",o.active())("aria-labelledby",o.ariaLabelledby()),u(o.cx("contentContainer")));},features:[P([m,{provide:De,useExisting:n},{provide:H,useExisting:n}]),D([d]),T],ngContentSelectors:L,decls:4,vars:10,consts:[["name","p-collapsible","hideStrategy","visibility",3,"visible","mountOnEnter","unmountOnLeave","options"],[3,"pBind"]],template:function(t,o){t&1&&(N(),B(0,"p-motion",0)(1,"div",1)(2,"div",1),x(3),k()()()),t&2&&(a("visible",o.active())("mountOnEnter",!1)("unmountOnLeave",!1)("options",o.computedMotionOptions()),l(),u(o.cx("contentWrapper")),a("pBind",o.ptm("contentWrapper",o.ptParams())),l(),u(o.cx("content")),a("pBind",o.ptm("content",o.ptParams())));},dependencies:[S,F,d,Ae,be],encapsulation:2,changeDetection:0});}return n;})(),Q=(()=>{class n extends O{componentName="Accordion";$pcAccordion=r(Te,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(d,{self:!0});onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}value=j(void 0);multiple=w(!1,{transform:e=>R(e)});styleClass;expandIcon;collapseIcon;selectOnFocus=w(!1,{transform:e=>R(e)});transitionOptions="400ms cubic-bezier(0.86, 0, 0.07, 1)";motionOptions=w(void 0);computedMotionOptions=c(()=>E(E({},this.ptm("motion")),this.motionOptions()));onClose=new V();onOpen=new V();id=X(pe("pn_id_"));_componentStyle=r(m);onKeydown(e){switch(e.code){case"ArrowDown":this.onTabArrowDownKey(e);break;case"ArrowUp":this.onTabArrowUpKey(e);break;case"Home":e.shiftKey||this.onTabHomeKey(e);break;case"End":e.shiftKey||this.onTabEndKey(e);break;}}onTabArrowDownKey(e){let t=this.findNextHeaderAction(e.target.parentElement);t?this.changeFocusedTab(t):this.onTabHomeKey(e),e.preventDefault();}onTabArrowUpKey(e){let t=this.findPrevHeaderAction(e.target.parentElement);t?this.changeFocusedTab(t):this.onTabEndKey(e),e.preventDefault();}onTabHomeKey(e){let t=this.findFirstHeaderAction();this.changeFocusedTab(t),e.preventDefault();}changeFocusedTab(e){e&&z(e);}findNextHeaderAction(e,t=!1){let o=t?e:e.nextElementSibling,i=y(o,'[data-pc-section="accordionheader"]');return i?M(i,"data-p-disabled")?this.findNextHeaderAction(i.parentElement):y(i.parentElement,'[data-pc-section="accordionheader"]'):null;}findPrevHeaderAction(e,t=!1){let o=t?e:e.previousElementSibling,i=y(o,'[data-pc-section="accordionheader"]');return i?M(i,"data-p-disabled")?this.findPrevHeaderAction(i.parentElement):y(i.parentElement,'[data-pc-section="accordionheader"]'):null;}findFirstHeaderAction(){let e=this.el.nativeElement.firstElementChild;return this.findNextHeaderAction(e,!0);}findLastHeaderAction(){let e=this.el.nativeElement.lastElementChild;return this.findPrevHeaderAction(e,!0);}onTabEndKey(e){let t=this.findLastHeaderAction();this.changeFocusedTab(t),e.preventDefault();}getBlockableElement(){return this.el.nativeElement.children[0];}updateValue(e){let t=this.value();if(this.multiple()){let o=Array.isArray(t)?[...t]:[],i=o.indexOf(e);i!==-1?o.splice(i,1):o.push(e),this.value.set(o);}else t===e?this.value.set(void 0):this.value.set(e);}static ɵfac=(()=>{let e;return function(o){return(e||(e=A(n)))(o||n);};})();static ɵcmp=g({type:n,selectors:[["p-accordion"]],hostVars:2,hostBindings:function(t,o){t&1&&I("keydown",function(p){return o.onKeydown(p);}),t&2&&u(o.cn(o.cx("root"),o.styleClass));},inputs:{value:[1,"value"],multiple:[1,"multiple"],styleClass:"styleClass",expandIcon:"expandIcon",collapseIcon:"collapseIcon",selectOnFocus:[1,"selectOnFocus"],transitionOptions:"transitionOptions",motionOptions:[1,"motionOptions"]},outputs:{value:"valueChange",onClose:"onClose",onOpen:"onOpen"},features:[P([m,{provide:Te,useExisting:n},{provide:H,useExisting:n}]),D([d]),T],ngContentSelectors:L,decls:1,vars:0,template:function(t,o){t&1&&(N(),x(0));},dependencies:[S,ue,F],encapsulation:2,changeDetection:0});}return n;})();var q={1:"Actiu",2:"Inactiu"},Tn=[{label:q[1],value:1},{label:q[2],value:2}],In="Informaci\xF3",Nn="La consulta de registres inactius encara no est\xE0 disponible.",xn="Funcionalitat pendent",Pn="La restauraci\xF3 de registres encara no est\xE0 implementada.";function wn(n){return q[n?2:1];}var Ne=class n{restore=de();PrimeIcons=G;actionsAriaLabel="Obrir les accions del registre inactiu";actions=[{label:"Restaura",icon:G.REFRESH,command:()=>this.restore.emit()}];static ɵfac=function(e){return new(e||n)();};static ɵcmp=g({type:n,selectors:[["app-restore-record-menu"]],outputs:{restore:"restore"},decls:3,vars:5,consts:[["menu",""],["severity","secondary",3,"onClick","text","icon","ariaLabel"],["appendTo","body",3,"model","popup"]],template:function(e,t){if(e&1){let o=ne();B(0,"p-button",1),I("onClick",function(p){Y(o);let xe=re(2);return J(xe.toggle(p));}),k(),v(1,"p-menu",2,0);}e&2&&(a("text",!0)("icon",t.PrimeIcons.ELLIPSIS_H)("ariaLabel",t.actionsAriaLabel),l(),a("model",t.actions)("popup",!0));},dependencies:[ve,_e],encapsulation:2,changeDetection:0});};export{q as a,Tn as b,In as c,Nn as d,xn as e,Pn as f,wn as g,Ne as h,Ie as i,bn as j,An as k,Q as l};/**i18n:eb83f67afd0d8d699696744481b8e0a9b71e68597e64d7e8f50a3892b99b2879*/