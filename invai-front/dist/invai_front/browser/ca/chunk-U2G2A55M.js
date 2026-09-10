import{i as ye}from"./chunk-XWNFYCQQ.js";import{a as fe}from"./chunk-QHTUDQ6A.js";import{b as he}from"./chunk-YKAKMDYW.js";import{Da as K,I as le,L as ue,N as F,O,U as d,V as k,ca as me,fa as ge,p as _,q as Q,r as T,xa as ve,za as be}from"./chunk-Y66MR3DC.js";import{C as q,G as pe}from"./chunk-QES7LLTG.js";import{d as de,f as se,i as M}from"./chunk-SV67CR7A.js";import{$b as ie,Ab as S,Ac as c,Bb as v,Fb as j,Fc as ce,Gb as $,Gc as H,Ib as ee,Jc as U,Pb as w,Qa as l,R as b,Rb as h,S as G,Sb as N,Tb as x,Ub as ne,V as D,Wb as oe,X as r,Xb as te,aa as W,ac as re,ba as J,ca as V,cb as g,dc as u,gb as E,ha as L,hb as I,ib as A,lc as P,ma as X,nc as ae,pb as f,ra as y,sb as Y,tb as Z,yb as a,zb as B}from"./chunk-PM3R7XGD.js";import{a as C}from"./chunk-TKJDIQEL.js";var Ae=`
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
`;var R=["*"],Pe=["toggleicon"],He=o=>({active:o});function Me(o,s){}function Te(o,s){o&1&&A(0,Me,0,0,"ng-template");}function Fe(o,s){if(o&1&&A(0,Te,1,0,null,0),o&2){let e=h();a("ngTemplateOutlet",e.toggleicon)("ngTemplateOutletContext",ae(2,He,e.active()));}}function Oe(o,s){if(o&1&&v(0,"span",4),o&2){let e=h(3);u(e.cn(e.cx("toggleicon"),e.pcAccordion.collapseIcon)),a("pBind",e.ptm("toggleicon")),f("aria-hidden",!0);}}function ke(o,s){if(o&1&&(V(),v(0,"svg",5)),o&2){let e=h(3);u(e.cx("toggleicon")),a("pBind",e.ptm("toggleicon")),f("aria-hidden",!0);}}function Be(o,s){if(o&1&&(j(0),A(1,Oe,1,4,"span",2)(2,ke,1,4,"svg",3),$()),o&2){let e=h(2);l(),a("ngIf",e.pcAccordion.collapseIcon),l(),a("ngIf",!e.pcAccordion.collapseIcon);}}function Se(o,s){if(o&1&&v(0,"span",4),o&2){let e=h(3);u(e.cn(e.cx("toggleicon"),e.pcAccordion.expandIcon)),a("pBind",e.ptm("toggleicon")),f("aria-hidden",!0);}}function Ke(o,s){if(o&1&&(V(),v(0,"svg",7)),o&2){let e=h(3);a("pBind",e.ptm("toggleicon")),f("aria-hidden",!0);}}function Re(o,s){if(o&1&&(j(0),A(1,Se,1,4,"span",2)(2,Ke,1,2,"svg",6),$()),o&2){let e=h(2);l(),a("ngIf",e.pcAccordion.expandIcon),l(),a("ngIf",!e.pcAccordion.expandIcon);}}function Ve(o,s){if(o&1&&A(0,Be,3,2,"ng-container",1)(1,Re,3,2,"ng-container",1),o&2){let e=h();a("ngIf",e.active()),l(),a("ngIf",!e.active());}}var Le=`
${Ae}

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
`,je={root:"p-accordion p-component",panel:({instance:o})=>["p-accordionpanel",{"p-accordionpanel-active":o.active(),"p-disabled":o.disabled()}],header:"p-accordionheader",toggleicon:"p-accordionheader-toggle-icon",contentContainer:"p-accordioncontent",contentWrapper:"p-accordioncontent-wrapper",content:"p-accordioncontent-content"},m=(()=>{class o extends ue{name="accordion";style=Le;classes=je;static ɵfac=(()=>{let e;return function(n){return(e||(e=y(o)))(n||o);};})();static ɵprov=G({token:o,factory:o.ɵfac});}return o;})();var _e=new D("ACCORDION_PANEL_INSTANCE"),Ce=new D("ACCORDION_HEADER_INSTANCE"),De=new D("ACCORDION_CONTENT_INSTANCE"),Ee=new D("ACCORDION_INSTANCE"),Ie=(()=>{class o extends O{$pcAccordionPanel=r(_e,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(d,{self:!0});componentName="AccordionPanel";onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}pcAccordion=r(b(()=>z));value=U(void 0);disabled=H(!1,{transform:e=>K(e)});active=c(()=>this.pcAccordion.multiple()?this.valueEquals(this.pcAccordion.value(),this.value()):this.pcAccordion.value()===this.value());valueEquals(e,t){return Array.isArray(e)?e.includes(t):e===t;}_componentStyle=r(m);static ɵfac=(()=>{let e;return function(n){return(e||(e=y(o)))(n||o);};})();static ɵcmp=g({type:o,selectors:[["p-accordion-panel"],["p-accordionpanel"]],hostVars:4,hostBindings:function(t,n){t&2&&(f("data-p-disabled",n.disabled())("data-p-active",n.active()),u(n.cx("panel")));},inputs:{value:[1,"value"],disabled:[1,"disabled"]},outputs:{value:"valueChange"},features:[P([m,{provide:_e,useExisting:o},{provide:F,useExisting:o}]),E([d]),I],ngContentSelectors:R,decls:1,vars:0,template:function(t,n){t&1&&(N(),x(0));},dependencies:[M,k],encapsulation:2,changeDetection:0});}return o;})(),vn=(()=>{class o extends O{$pcAccordionHeader=r(Ce,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(d,{self:!0});componentName="AccordionHeader";onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}pcAccordion=r(b(()=>z));pcAccordionPanel=r(b(()=>Ie));id=c(()=>`${this.pcAccordion.id()}_accordionheader_${this.pcAccordionPanel.value()}`);active=c(()=>this.pcAccordionPanel.active());disabled=c(()=>this.pcAccordionPanel.disabled());ariaControls=c(()=>`${this.pcAccordion.id()}_accordioncontent_${this.pcAccordionPanel.value()}`);toggleicon;onClick(e){if(this.disabled())return;let t=this.active();this.changeActiveValue();let n=this.active(),i=this.pcAccordionPanel.value();!t&&n?this.pcAccordion.onOpen.emit({originalEvent:e,index:i}):t&&!n&&this.pcAccordion.onClose.emit({originalEvent:e,index:i});}onFocus(){!this.disabled()&&this.pcAccordion.selectOnFocus()&&this.changeActiveValue();}onKeydown(e){switch(e.code){case"ArrowDown":this.arrowDownKey(e);break;case"ArrowUp":this.arrowUpKey(e);break;case"Home":this.onHomeKey(e);break;case"End":this.onEndKey(e);break;case"Enter":case"Space":case"NumpadEnter":this.onEnterKey(e);break;default:break;}}_componentStyle=r(m);changeActiveValue(){this.pcAccordion.updateValue(this.pcAccordionPanel.value());}findPanel(e){return e?.closest('[data-pc-name="accordionpanel"]');}findHeader(e){return _(e,'[data-pc-name="accordionheader"]');}findNextPanel(e,t=!1){let n=t?e:e.nextElementSibling;return n?T(n,"data-p-disabled")?this.findNextPanel(n):this.findHeader(n):null;}findPrevPanel(e,t=!1){let n=t?e:e.previousElementSibling;return n?T(n,"data-p-disabled")?this.findPrevPanel(n):this.findHeader(n):null;}findFirstPanel(){return this.findNextPanel(this.pcAccordion.el.nativeElement.firstElementChild,!0);}findLastPanel(){return this.findPrevPanel(this.pcAccordion.el.nativeElement.lastElementChild,!0);}changeFocusedPanel(e,t){Q(t);}arrowDownKey(e){let t=this.findNextPanel(this.findPanel(e.currentTarget));t?this.changeFocusedPanel(e,t):this.onHomeKey(e),e.preventDefault();}arrowUpKey(e){let t=this.findPrevPanel(this.findPanel(e.currentTarget));t?this.changeFocusedPanel(e,t):this.onEndKey(e),e.preventDefault();}onHomeKey(e){let t=this.findFirstPanel();this.changeFocusedPanel(e,t),e.preventDefault();}onEndKey(e){let t=this.findLastPanel();this.changeFocusedPanel(e,t),e.preventDefault();}onEnterKey(e){this.disabled()||this.changeActiveValue(),e.preventDefault();}get dataP(){return this.cn({active:this.active()});}static ɵfac=(()=>{let e;return function(n){return(e||(e=y(o)))(n||o);};})();static ɵcmp=g({type:o,selectors:[["p-accordion-header"],["p-accordionheader"]],contentQueries:function(t,n,i){if(t&1&&ne(i,Pe,5),t&2){let p;oe(p=te())&&(n.toggleicon=p.first);}},hostVars:13,hostBindings:function(t,n){t&1&&w("click",function(p){return n.onClick(p);})("focus",function(){return n.onFocus();})("keydown",function(p){return n.onKeydown(p);}),t&2&&(f("id",n.id())("aria-expanded",n.active())("aria-controls",n.ariaControls())("aria-disabled",n.disabled())("role","button")("tabindex",n.disabled()?"-1":"0")("data-p-active",n.active())("data-p-disabled",n.disabled())("data-p",n.dataP),u(n.cx("header")),re("user-select","none"));},features:[P([m,{provide:Ce,useExisting:o},{provide:F,useExisting:o}]),E([me,d]),I],ngContentSelectors:R,decls:3,vars:1,consts:[[4,"ngTemplateOutlet","ngTemplateOutletContext"],[4,"ngIf"],[3,"class","pBind",4,"ngIf"],["data-p-icon","chevron-up",3,"class","pBind",4,"ngIf"],[3,"pBind"],["data-p-icon","chevron-up",3,"pBind"],["data-p-icon","chevron-down",3,"pBind",4,"ngIf"],["data-p-icon","chevron-down",3,"pBind"]],template:function(t,n){t&1&&(N(),x(0),Y(1,Fe,1,4)(2,Ve,2,2)),t&2&&(l(),Z(n.toggleicon?1:2));},dependencies:[M,de,se,he,fe,k,d],encapsulation:2,changeDetection:0});}return o;})(),bn=(()=>{class o extends O{$pcAccordionContent=r(De,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(d,{self:!0});componentName="AccordionContent";onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}pcAccordion=r(b(()=>z));pcAccordionPanel=r(b(()=>Ie));active=c(()=>this.pcAccordionPanel.active());ariaLabelledby=c(()=>`${this.pcAccordion.id()}_accordionheader_${this.pcAccordionPanel.value()}`);id=c(()=>`${this.pcAccordion.id()}_accordioncontent_${this.pcAccordionPanel.value()}`);_componentStyle=r(m);ptParams=c(()=>({context:this.active()}));computedMotionOptions=c(()=>C(C({},this.ptm("motion",this.ptParams())),this.pcAccordion.computedMotionOptions()));static ɵfac=(()=>{let e;return function(n){return(e||(e=y(o)))(n||o);};})();static ɵcmp=g({type:o,selectors:[["p-accordion-content"],["p-accordioncontent"]],hostVars:6,hostBindings:function(t,n){t&2&&(f("id",n.id())("role","region")("data-p-active",n.active())("aria-labelledby",n.ariaLabelledby()),u(n.cx("contentContainer")));},features:[P([m,{provide:De,useExisting:o},{provide:F,useExisting:o}]),E([d]),I],ngContentSelectors:R,decls:4,vars:10,consts:[["name","p-collapsible","hideStrategy","visibility",3,"visible","mountOnEnter","unmountOnLeave","options"],[3,"pBind"]],template:function(t,n){t&1&&(N(),B(0,"p-motion",0)(1,"div",1)(2,"div",1),x(3),S()()()),t&2&&(a("visible",n.active())("mountOnEnter",!1)("unmountOnLeave",!1)("options",n.computedMotionOptions()),l(),u(n.cx("contentWrapper")),a("pBind",n.ptm("contentWrapper",n.ptParams())),l(),u(n.cx("content")),a("pBind",n.ptm("content",n.ptParams())));},dependencies:[M,k,d,be,ve],encapsulation:2,changeDetection:0});}return o;})(),z=(()=>{class o extends O{componentName="Accordion";$pcAccordion=r(Ee,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(d,{self:!0});onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}value=U(void 0);multiple=H(!1,{transform:e=>K(e)});styleClass;expandIcon;collapseIcon;selectOnFocus=H(!1,{transform:e=>K(e)});transitionOptions="400ms cubic-bezier(0.86, 0, 0.07, 1)";motionOptions=H(void 0);computedMotionOptions=c(()=>C(C({},this.ptm("motion")),this.motionOptions()));onClose=new L();onOpen=new L();id=X(le("pn_id_"));_componentStyle=r(m);onKeydown(e){switch(e.code){case"ArrowDown":this.onTabArrowDownKey(e);break;case"ArrowUp":this.onTabArrowUpKey(e);break;case"Home":e.shiftKey||this.onTabHomeKey(e);break;case"End":e.shiftKey||this.onTabEndKey(e);break;}}onTabArrowDownKey(e){let t=this.findNextHeaderAction(e.target.parentElement);t?this.changeFocusedTab(t):this.onTabHomeKey(e),e.preventDefault();}onTabArrowUpKey(e){let t=this.findPrevHeaderAction(e.target.parentElement);t?this.changeFocusedTab(t):this.onTabEndKey(e),e.preventDefault();}onTabHomeKey(e){let t=this.findFirstHeaderAction();this.changeFocusedTab(t),e.preventDefault();}changeFocusedTab(e){e&&Q(e);}findNextHeaderAction(e,t=!1){let n=t?e:e.nextElementSibling,i=_(n,'[data-pc-section="accordionheader"]');return i?T(i,"data-p-disabled")?this.findNextHeaderAction(i.parentElement):_(i.parentElement,'[data-pc-section="accordionheader"]'):null;}findPrevHeaderAction(e,t=!1){let n=t?e:e.previousElementSibling,i=_(n,'[data-pc-section="accordionheader"]');return i?T(i,"data-p-disabled")?this.findPrevHeaderAction(i.parentElement):_(i.parentElement,'[data-pc-section="accordionheader"]'):null;}findFirstHeaderAction(){let e=this.el.nativeElement.firstElementChild;return this.findNextHeaderAction(e,!0);}findLastHeaderAction(){let e=this.el.nativeElement.lastElementChild;return this.findPrevHeaderAction(e,!0);}onTabEndKey(e){let t=this.findLastHeaderAction();this.changeFocusedTab(t),e.preventDefault();}getBlockableElement(){return this.el.nativeElement.children[0];}updateValue(e){let t=this.value();if(this.multiple()){let n=Array.isArray(t)?[...t]:[],i=n.indexOf(e);i!==-1?n.splice(i,1):n.push(e),this.value.set(n);}else t===e?this.value.set(void 0):this.value.set(e);}static ɵfac=(()=>{let e;return function(n){return(e||(e=y(o)))(n||o);};})();static ɵcmp=g({type:o,selectors:[["p-accordion"]],hostVars:2,hostBindings:function(t,n){t&1&&w("keydown",function(p){return n.onKeydown(p);}),t&2&&u(n.cn(n.cx("root"),n.styleClass));},inputs:{value:[1,"value"],multiple:[1,"multiple"],styleClass:"styleClass",expandIcon:"expandIcon",collapseIcon:"collapseIcon",selectOnFocus:[1,"selectOnFocus"],transitionOptions:"transitionOptions",motionOptions:[1,"motionOptions"]},outputs:{value:"valueChange",onClose:"onClose",onOpen:"onOpen"},features:[P([m,{provide:Ee,useExisting:o},{provide:F,useExisting:o}]),E([d]),I],ngContentSelectors:R,decls:1,vars:0,template:function(t,n){t&1&&(N(),x(0));},dependencies:[M,pe,k],encapsulation:2,changeDetection:0});}return o;})();var we=class o{restore=ce();PrimeIcons=q;actionsAriaLabel="Obrir les accions del registre inactiu";actions=[{label:"Restaura",icon:q.REFRESH,command:()=>this.restore.emit()}];static ɵfac=function(e){return new(e||o)();};static ɵcmp=g({type:o,selectors:[["app-restore-record-menu"]],outputs:{restore:"restore"},decls:3,vars:5,consts:[["menu",""],["severity","secondary",3,"onClick","text","icon","ariaLabel"],["appendTo","body",3,"model","popup"]],template:function(e,t){if(e&1){let n=ee();B(0,"p-button",1),w("onClick",function(p){W(n);let Ne=ie(2);return J(Ne.toggle(p));}),S(),v(1,"p-menu",2,0);}e&2&&(a("text",!0)("icon",t.PrimeIcons.ELLIPSIS_H)("ariaLabel",t.actionsAriaLabel),l(),a("model",t.actions)("popup",!0));},dependencies:[ge,ye],encapsulation:2,changeDetection:0});};export{we as a,Ie as b,vn as c,bn as d,z as e};/**i18n:85b369cd519586902b7f4f17a6e24b5cb5057fd4d248a08016454ba7f85e36fe*/