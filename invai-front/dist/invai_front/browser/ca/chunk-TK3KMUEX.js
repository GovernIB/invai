import{a as se,e as le}from"./chunk-2CL75N43.js";import{T as oe,V as ae,aa as m,ba as re,ga as de}from"./chunk-A7IAJ6XT.js";import{F as te,G as V,f as ee,g as ne}from"./chunk-COF5ICWG.js";import{c as ie,g as ce}from"./chunk-LIE47PH6.js";import{k as Z,m as J,o as W,r as Y}from"./chunk-SM2AXIHS.js";import{Bb as c,Cb as M,Db as T,Dc as X,Eb as I,Hb as L,Ib as E,Jb as S,Jc as F,Lb as Q,Rc as x,Sb as j,Sc as U,Ta as d,U as N,Ub as h,V as z,W as D,Xb as H,Y as A,Yb as R,Zb as w,_ as f,_b as B,da as g,ea as v,fa as u,fb as _,fc as q,gb as O,gc as l,jb as $,ka as C,kb as y,lb as k,oc as G,pa as P,sb as s,sc as K,ua as b}from"./chunk-26JRJ62F.js";var me=["data-p-icon","minus"],he=(()=>{class e extends de{static \u0275fac=(()=>{let n;return function(t){return(n||(n=b(e)))(t||e)}})();static \u0275cmp=_({type:e,selectors:[["","data-p-icon","minus"]],features:[y],attrs:me,decls:1,vars:0,consts:[["d","M13.2222 7.77778H0.777778C0.571498 7.77778 0.373667 7.69584 0.227806 7.54998C0.0819442 7.40412 0 7.20629 0 7.00001C0 6.79373 0.0819442 6.5959 0.227806 6.45003C0.373667 6.30417 0.571498 6.22223 0.777778 6.22223H13.2222C13.4285 6.22223 13.6263 6.30417 13.7722 6.45003C13.9181 6.5959 14 6.79373 14 7.00001C14 7.20629 13.9181 7.40412 13.7722 7.54998C13.6263 7.69584 13.4285 7.77778 13.2222 7.77778Z","fill","currentColor"]],template:function(o,t){o&1&&(u(),L(0,"path",0))},encapsulation:2})}return e})();var pe=`
    .p-checkbox {
        position: relative;
        display: inline-flex;
        user-select: none;
        vertical-align: bottom;
        width: dt('checkbox.width');
        height: dt('checkbox.height');
    }

    .p-checkbox-input {
        cursor: pointer;
        appearance: none;
        position: absolute;
        inset-block-start: 0;
        inset-inline-start: 0;
        width: 100%;
        height: 100%;
        padding: 0;
        margin: 0;
        opacity: 0;
        z-index: 1;
        outline: 0 none;
        border: 1px solid transparent;
        border-radius: dt('checkbox.border.radius');
    }

    .p-checkbox-box {
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: dt('checkbox.border.radius');
        border: 1px solid dt('checkbox.border.color');
        background: dt('checkbox.background');
        width: dt('checkbox.width');
        height: dt('checkbox.height');
        transition:
            background dt('checkbox.transition.duration'),
            color dt('checkbox.transition.duration'),
            border-color dt('checkbox.transition.duration'),
            box-shadow dt('checkbox.transition.duration'),
            outline-color dt('checkbox.transition.duration');
        outline-color: transparent;
        box-shadow: dt('checkbox.shadow');
    }

    .p-checkbox-icon {
        transition-duration: dt('checkbox.transition.duration');
        color: dt('checkbox.icon.color');
        font-size: dt('checkbox.icon.size');
        width: dt('checkbox.icon.size');
        height: dt('checkbox.icon.size');
    }

    .p-checkbox:not(.p-disabled):has(.p-checkbox-input:hover) .p-checkbox-box {
        border-color: dt('checkbox.hover.border.color');
    }

    .p-checkbox-checked .p-checkbox-box {
        border-color: dt('checkbox.checked.border.color');
        background: dt('checkbox.checked.background');
    }

    .p-checkbox-checked .p-checkbox-icon {
        color: dt('checkbox.icon.checked.color');
    }

    .p-checkbox-checked:not(.p-disabled):has(.p-checkbox-input:hover) .p-checkbox-box {
        background: dt('checkbox.checked.hover.background');
        border-color: dt('checkbox.checked.hover.border.color');
    }

    .p-checkbox-checked:not(.p-disabled):has(.p-checkbox-input:hover) .p-checkbox-icon {
        color: dt('checkbox.icon.checked.hover.color');
    }

    .p-checkbox:not(.p-disabled):has(.p-checkbox-input:focus-visible) .p-checkbox-box {
        border-color: dt('checkbox.focus.border.color');
        box-shadow: dt('checkbox.focus.ring.shadow');
        outline: dt('checkbox.focus.ring.width') dt('checkbox.focus.ring.style') dt('checkbox.focus.ring.color');
        outline-offset: dt('checkbox.focus.ring.offset');
    }

    .p-checkbox-checked:not(.p-disabled):has(.p-checkbox-input:focus-visible) .p-checkbox-box {
        border-color: dt('checkbox.checked.focus.border.color');
    }

    .p-checkbox.p-invalid > .p-checkbox-box {
        border-color: dt('checkbox.invalid.border.color');
    }

    .p-checkbox.p-variant-filled .p-checkbox-box {
        background: dt('checkbox.filled.background');
    }

    .p-checkbox-checked.p-variant-filled .p-checkbox-box {
        background: dt('checkbox.checked.background');
    }

    .p-checkbox-checked.p-variant-filled:not(.p-disabled):has(.p-checkbox-input:hover) .p-checkbox-box {
        background: dt('checkbox.checked.hover.background');
    }

    .p-checkbox.p-disabled {
        opacity: 1;
    }

    .p-checkbox.p-disabled .p-checkbox-box {
        background: dt('checkbox.disabled.background');
        border-color: dt('checkbox.checked.disabled.border.color');
    }

    .p-checkbox.p-disabled .p-checkbox-box .p-checkbox-icon {
        color: dt('checkbox.icon.disabled.color');
    }

    .p-checkbox-sm,
    .p-checkbox-sm .p-checkbox-box {
        width: dt('checkbox.sm.width');
        height: dt('checkbox.sm.height');
    }

    .p-checkbox-sm .p-checkbox-icon {
        font-size: dt('checkbox.icon.sm.size');
        width: dt('checkbox.icon.sm.size');
        height: dt('checkbox.icon.sm.size');
    }

    .p-checkbox-lg,
    .p-checkbox-lg .p-checkbox-box {
        width: dt('checkbox.lg.width');
        height: dt('checkbox.lg.height');
    }

    .p-checkbox-lg .p-checkbox-icon {
        font-size: dt('checkbox.icon.lg.size');
        width: dt('checkbox.icon.lg.size');
        height: dt('checkbox.icon.lg.size');
    }
`;var fe=["icon"],ge=["input"],ve=(e,r,n)=>({checked:e,class:r,dataP:n});function Ce(e,r){if(e&1&&I(0,"span",8),e&2){let n=h(3);l(n.cx("icon")),c("ngClass",n.checkboxIcon)("pBind",n.ptm("icon")),s("data-p",n.dataP)}}function _e(e,r){if(e&1&&(u(),I(0,"svg",9)),e&2){let n=h(3);l(n.cx("icon")),c("pBind",n.ptm("icon")),s("data-p",n.dataP)}}function ye(e,r){if(e&1&&(E(0),k(1,Ce,1,5,"span",6)(2,_e,1,4,"svg",7),S()),e&2){let n=h(2);d(),c("ngIf",n.checkboxIcon),d(),c("ngIf",!n.checkboxIcon)}}function Ie(e,r){if(e&1&&(u(),I(0,"svg",10)),e&2){let n=h(2);l(n.cx("icon")),c("pBind",n.ptm("icon")),s("data-p",n.dataP)}}function we(e,r){if(e&1&&(E(0),k(1,ye,3,2,"ng-container",3)(2,Ie,1,4,"svg",5),S()),e&2){let n=h();d(),c("ngIf",n.checked),d(),c("ngIf",n._indeterminate())}}function Be(e,r){}function Ve(e,r){e&1&&k(0,Be,0,0,"ng-template")}var Me=`
    ${pe}

    /* For PrimeNG */
    p-checkBox.ng-invalid.ng-dirty .p-checkbox-box,
    p-check-box.ng-invalid.ng-dirty .p-checkbox-box,
    p-checkbox.ng-invalid.ng-dirty .p-checkbox-box {
        border-color: dt('checkbox.invalid.border.color');
    }
`,Te={root:({instance:e})=>["p-checkbox p-component",{"p-checkbox-checked p-highlight":e.checked,"p-disabled":e.$disabled(),"p-invalid":e.invalid(),"p-variant-filled":e.$variant()==="filled","p-checkbox-sm p-inputfield-sm":e.size()==="small","p-checkbox-lg p-inputfield-lg":e.size()==="large"}],box:"p-checkbox-box",input:"p-checkbox-input",icon:"p-checkbox-icon"},ue=(()=>{class e extends oe{name="checkbox";style=Me;classes=Te;static \u0275fac=(()=>{let n;return function(t){return(n||(n=b(e)))(t||e)}})();static \u0275prov=z({token:e,factory:e.\u0275fac})}return e})();var be=new A("CHECKBOX_INSTANCE"),Ee={provide:ie,useExisting:N(()=>ke),multi:!0},ke=(()=>{class e extends le{componentName="Checkbox";hostName="";value;binary;ariaLabelledBy;ariaLabel;tabindex;inputId;inputStyle;styleClass;inputClass;indeterminate=!1;formControl;checkboxIcon;readonly;autofocus;trueValue=!0;falseValue=!1;variant=F();size=F();onChange=new C;onFocus=new C;onBlur=new C;inputViewChild;get checked(){return this._indeterminate()?!1:this.binary?this.modelValue()===this.trueValue:ne(this.value,this.modelValue())}_indeterminate=P(void 0);checkboxIconTemplate;templates;_checkboxIconTemplate;focused=!1;_componentStyle=f(ue);bindDirectiveInstance=f(m,{self:!0});$pcCheckbox=f(be,{optional:!0,skipSelf:!0})??void 0;$variant=X(()=>this.variant()||this.config.inputStyle()||this.config.inputVariant());onAfterContentInit(){this.templates?.forEach(n=>{switch(n.getType()){case"icon":this._checkboxIconTemplate=n.template;break;case"checkboxicon":this._checkboxIconTemplate=n.template;break}})}onChanges(n){n.indeterminate&&this._indeterminate.set(n.indeterminate.currentValue)}onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptms(["host","root"]))}updateModel(n){let o,t=this.injector.get(ce,null,{optional:!0,self:!0}),i=t&&!this.formControl?t.value:this.modelValue();this.binary?(o=this._indeterminate()?this.trueValue:this.checked?this.falseValue:this.trueValue,this.writeModelValue(o),this.onModelChange(o)):(this.checked||this._indeterminate()?o=i.filter(a=>!ee(a,this.value)):o=i?[...i,this.value]:[this.value],this.onModelChange(o),this.writeModelValue(o),this.formControl&&this.formControl.setValue(o)),this._indeterminate()&&this._indeterminate.set(!1),this.onChange.emit({checked:o,originalEvent:n})}handleChange(n){this.readonly||this.updateModel(n)}onInputFocus(n){this.focused=!0,this.onFocus.emit(n)}onInputBlur(n){this.focused=!1,this.onBlur.emit(n),this.onModelTouched()}focus(){this.inputViewChild?.nativeElement.focus()}writeControlValue(n,o){o(n),this.cd.markForCheck()}get dataP(){return this.cn({invalid:this.invalid(),checked:this.checked,disabled:this.$disabled(),filled:this.$variant()==="filled",[this.size()]:this.size()})}static \u0275fac=(()=>{let n;return function(t){return(n||(n=b(e)))(t||e)}})();static \u0275cmp=_({type:e,selectors:[["p-checkbox"],["p-checkBox"],["p-check-box"]],contentQueries:function(o,t,i){if(o&1&&H(i,fe,4)(i,te,4),o&2){let a;w(a=B())&&(t.checkboxIconTemplate=a.first),w(a=B())&&(t.templates=a)}},viewQuery:function(o,t){if(o&1&&R(ge,5),o&2){let i;w(i=B())&&(t.inputViewChild=i.first)}},hostVars:6,hostBindings:function(o,t){o&2&&(s("data-p-highlight",t.checked)("data-p-checked",t.checked)("data-p-disabled",t.$disabled())("data-p",t.dataP),l(t.cn(t.cx("root"),t.styleClass)))},inputs:{hostName:"hostName",value:"value",binary:[2,"binary","binary",x],ariaLabelledBy:"ariaLabelledBy",ariaLabel:"ariaLabel",tabindex:[2,"tabindex","tabindex",U],inputId:"inputId",inputStyle:"inputStyle",styleClass:"styleClass",inputClass:"inputClass",indeterminate:[2,"indeterminate","indeterminate",x],formControl:"formControl",checkboxIcon:"checkboxIcon",readonly:[2,"readonly","readonly",x],autofocus:[2,"autofocus","autofocus",x],trueValue:"trueValue",falseValue:"falseValue",variant:[1,"variant"],size:[1,"size"]},outputs:{onChange:"onChange",onFocus:"onFocus",onBlur:"onBlur"},features:[G([Ee,ue,{provide:be,useExisting:e},{provide:ae,useExisting:e}]),$([m]),y],decls:5,vars:26,consts:[["input",""],["type","checkbox",3,"focus","blur","change","checked","pBind"],[3,"pBind"],[4,"ngIf"],[4,"ngTemplateOutlet","ngTemplateOutletContext"],["data-p-icon","minus",3,"class","pBind",4,"ngIf"],[3,"class","ngClass","pBind",4,"ngIf"],["data-p-icon","check",3,"class","pBind",4,"ngIf"],[3,"ngClass","pBind"],["data-p-icon","check",3,"pBind"],["data-p-icon","minus",3,"pBind"]],template:function(o,t){if(o&1){let i=Q();M(0,"input",1,0),j("focus",function(p){return g(i),v(t.onInputFocus(p))})("blur",function(p){return g(i),v(t.onInputBlur(p))})("change",function(p){return g(i),v(t.handleChange(p))}),T(),M(2,"div",2),k(3,we,3,2,"ng-container",3)(4,Ve,1,0,null,4),T()}o&2&&(q(t.inputStyle),l(t.cn(t.cx("input"),t.inputClass)),c("checked",t.checked)("pBind",t.ptm("input")),s("id",t.inputId)("value",t.value)("name",t.name())("tabindex",t.tabindex)("required",t.required()?"":void 0)("readonly",t.readonly?"":void 0)("disabled",t.$disabled()?"":void 0)("aria-labelledby",t.ariaLabelledBy)("aria-label",t.ariaLabel),d(2),l(t.cx("box")),c("pBind",t.ptm("box")),s("data-p",t.dataP),d(),c("ngIf",!t.checkboxIconTemplate&&!t._checkboxIconTemplate),d(),c("ngTemplateOutlet",t.checkboxIconTemplate||t._checkboxIconTemplate)("ngTemplateOutletContext",K(22,ve,t.checked,t.cx("icon"),t.dataP)))},dependencies:[Y,Z,J,W,V,se,he,re,m],encapsulation:2,changeDetection:0})}return e})(),cn=(()=>{class e{static \u0275fac=function(o){return new(o||e)};static \u0275mod=O({type:e});static \u0275inj=D({imports:[ke,V,V]})}return e})();export{ke as a,cn as b};
/**i18n:e8136fae3ab93b16771db13725b428f9aac2934b6934bfc6b4b028b903db9add*/
